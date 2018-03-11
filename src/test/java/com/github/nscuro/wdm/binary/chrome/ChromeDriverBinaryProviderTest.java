package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.LINUX64;
import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.MAC64;
import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.WIN32;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ChromeDriverBinaryProviderTest {

    private HttpClient httpClientMock;

    private GoogleCloudStorageDirectory cloudStorageDirectoryMock;

    private BinaryExtractorFactory binaryExtractorFactoryMock;

    private ChromeDriverBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        httpClientMock = mock(HttpClient.class);

        cloudStorageDirectoryMock = mock(GoogleCloudStorageDirectory.class);

        binaryExtractorFactoryMock = mock(BinaryExtractorFactory.class);

        binaryProvider = new ChromeDriverBinaryProvider(httpClientMock, cloudStorageDirectoryMock, binaryExtractorFactoryMock);
    }

    @Nested
    class ProvidesBinaryForBrowserTest {

        @Test
        void shouldReturnTrueForChromeBrowser() {
            assertThat(binaryProvider.providesBinaryForBrowser(Browser.CHROME)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(Browser.class)
        void shouldReturnFalseForEveryBrowserExceptChrome(final Browser browser) {
            assumeFalse(Browser.CHROME == browser);

            assertThat(binaryProvider.providesBinaryForBrowser(browser)).isFalse();
        }

    }

    @Nested
    class GetLatestBinaryVersionTest {

        private final GoogleCloudStorageEntry LATEST_RELEASE_ENTRY =
                new GoogleCloudStorageEntry("LATEST_RELEASE", "LATEST_RELEASE_URL", null);

        @Test
        void shouldReturnLatestAvailableVersionNotHigherThanSuggestedByReleaseFile() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Arrays.asList(
                            new GoogleCloudStorageEntry(format("1.1/%s", MAC64), null, null),
                            new GoogleCloudStorageEntry(format("1.1/%s", LINUX64), null, null),
                            new GoogleCloudStorageEntry(format("1.2/%s", MAC64), null, null),
                            new GoogleCloudStorageEntry(format("1.3/%s", MAC64), null, null),
                            new GoogleCloudStorageEntry(format("1.3/%s", LINUX64), null, null),
                            LATEST_RELEASE_ENTRY
                    ));

            //noinspection unchecked
            given(httpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class)))
                    .willReturn("1.2");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.MACOS, Architecture.X64))
                    .hasValue("1.2");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X64))
                    .hasValue("1.1");
        }

        @Test
        void shouldThrowExceptionWhenLatestReleaseFileCannotBeFound() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(emptyList());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64));
        }

        @Test
        void shouldReturnEmptyOptionalWhenPlatformIsNotSupported() throws IOException {
            assertThat(binaryProvider.getLatestBinaryVersion(Os.MACOS, Architecture.X86))
                    .isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenPlatformDoesNotMatch() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Arrays.asList(
                            new GoogleCloudStorageEntry(format("2.2/%s", WIN32), null, null),
                            LATEST_RELEASE_ENTRY)
                    );

            //noinspection unchecked
            given(httpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class)))
                    .willReturn("2.2");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X64))
                    .isEmpty();
        }

    }

}