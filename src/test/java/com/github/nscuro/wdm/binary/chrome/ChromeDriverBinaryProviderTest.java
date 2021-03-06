package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectoryService;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.LINUX64;
import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.MAC64;
import static com.github.nscuro.wdm.binary.chrome.ChromeDriverPlatform.WIN32;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("The ChromeDriver BinaryProvider")
class ChromeDriverBinaryProviderTest {

    private HttpClient httpClientMock;

    private GoogleCloudStorageDirectoryService cloudStorageDirectoryMock;

    private BinaryExtractorFactory binaryExtractorFactoryMock;

    private ChromeDriverBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        httpClientMock = mock(HttpClient.class);

        cloudStorageDirectoryMock = mock(GoogleCloudStorageDirectoryService.class);

        binaryExtractorFactoryMock = mock(BinaryExtractorFactory.class);

        binaryProvider = new ChromeDriverBinaryProvider(httpClientMock, cloudStorageDirectoryMock, binaryExtractorFactoryMock);
    }

    @Nested
    @DisplayName("when being instantiated")
    class PublicConstructorTest {

        @Test
        @DisplayName("should throw an exception when no HttpClient was provided")
        void shouldThrowExceptionWhenNoHttpClientIsProvided() {
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> new ChromeDriverBinaryProvider(null));
        }

    }

    @Nested
    @DisplayName("when indicating browser support")
    class ProvidesBinaryForBrowserTest {

        @Test
        @DisplayName("should return true for Chrome")
        void shouldReturnTrueForChromeBrowser() {
            assertThat(binaryProvider.providesBinaryForBrowser(Browser.CHROME)).isTrue();
        }

        @ParameterizedTest(name = "[{index}] browser={0}")
        @EnumSource(value = Browser.class, mode = Mode.EXCLUDE, names = "CHROME")
        @DisplayName("should return false for every browser except Chrome")
        void shouldReturnFalseForEveryBrowserExceptChrome(final Browser browser) {
            assertThat(binaryProvider.providesBinaryForBrowser(browser)).isFalse();
        }

    }

    @Nested
    @DisplayName("when determining the latest binary version")
    class GetLatestBinaryVersionTest {

        private final GoogleCloudStorageEntry LATEST_RELEASE_ENTRY =
                new GoogleCloudStorageEntry("LATEST_RELEASE", "LATEST_RELEASE_URL");

        @Test
        @DisplayName("should return latest available version that is not higher than that suggested by the LATEST_RELEASE file")
        void shouldReturnLatestAvailableVersionNotHigherThanSuggestedByReleaseFile() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Arrays.asList(
                            new GoogleCloudStorageEntry(format("1.1/%s", MAC64), null),
                            new GoogleCloudStorageEntry(format("1.1/%s", LINUX64), null),
                            new GoogleCloudStorageEntry(format("1.2/%s", MAC64), null),
                            new GoogleCloudStorageEntry(format("1.3/%s", MAC64), null),
                            new GoogleCloudStorageEntry(format("1.3/%s", LINUX64), null),
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
        @DisplayName("should throw an exception when LATEST_RELEASE file couldn't be found")
        void shouldThrowExceptionWhenLatestReleaseFileCouldNotBeFound() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(emptyList());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64));
        }

        @Test
        @DisplayName("should return an empty Optional when the desired platform is not supported")
        void shouldReturnEmptyOptionalWhenPlatformIsNotSupported() throws IOException {
            assertThat(binaryProvider.getLatestBinaryVersion(Os.MACOS, Architecture.X86))
                    .isNotPresent();
        }

        @Test
        @DisplayName("should return an empty Optional when no binary is available for the desired platform")
        void shouldReturnEmptyOptionalWhenPlatformDoesNotMatch() throws IOException {
            given(cloudStorageDirectoryMock.getEntries())
                    .willReturn(Arrays.asList(
                            new GoogleCloudStorageEntry(format("2.2/%s", WIN32), null),
                            LATEST_RELEASE_ENTRY)
                    );

            //noinspection unchecked
            given(httpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class)))
                    .willReturn("2.2");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.LINUX, Architecture.X64))
                    .isEmpty();
        }

    }

    @Nested
    @DisplayName("when checking equality")
    class EqualsTest {

        @Test
        @DisplayName("should return false when being compared with null")
        void shouldReturnFalseWhenComparedWithNull() {
            assertThat(binaryProvider.equals(null)).isFalse();
        }

        @Test
        @DisplayName("should return true when being compared with itself")
        void shouldReturnTrueWhenComparedWithItself() {
            assertThat(binaryProvider.equals(binaryProvider)).isTrue();
        }

        @Test
        @DisplayName("should return false when being compared with object that is not a BinaryProvider")
        void shouldReturnFalseWhenComparedWithNonBinaryProvider() {
            assertThat(binaryProvider.equals("noBinaryProvider")).isFalse();
        }

        @Test
        @DisplayName("should return true when being compared with a BinaryProvider that provides binaries for the same browser")
        void shouldReturnTrueWhenComparedWithBinaryProviderThatProvidesBinaryForSameBrowser() {
            final BinaryProvider otherBinaryProvider = new BinaryProvider() {
                @Override
                public boolean providesBinaryForBrowser(final Browser browser) {
                    return Browser.CHROME == browser;
                }

                @Nonnull
                @Override
                public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) {
                    throw new UnsupportedOperationException();
                }

                @Nonnull
                @Override
                public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) {
                    throw new UnsupportedOperationException();
                }
            };

            assertThat(binaryProvider.equals(otherBinaryProvider)).isTrue();
        }

    }

    @Test
    @DisplayName("should have the same hashCode as the Chrome browser")
    void shouldHaveTheSameHashCodeAsChromeBrowser() {
        assertThat(binaryProvider.hashCode()).isEqualTo(Browser.CHROME.hashCode());
    }

}