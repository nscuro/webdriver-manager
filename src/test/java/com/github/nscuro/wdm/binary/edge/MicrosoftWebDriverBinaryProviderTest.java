package com.github.nscuro.wdm.binary.edge;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("The Microsoft WebDriver BinaryProvider")
class MicrosoftWebDriverBinaryProviderTest {

    private static final String TEST_BASE_URL = "http://localhost/";

    private HttpClient httpClientMock;

    private MicrosoftWebDriverBinaryProvider binaryProvider;

    @BeforeEach
    void beforeEach() {
        httpClientMock = mock(HttpClient.class);

        binaryProvider = new MicrosoftWebDriverBinaryProvider(httpClientMock, TEST_BASE_URL);
    }

    @Nested
    @DisplayName("when being instantiated")
    class PublicConstructorTest {

        @Test
        @DisplayName("should throw an exception when no HttpClient was provided")
        void shouldThrowExceptionWhenNoHttpClientIsProvided() {
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> new MicrosoftWebDriverBinaryProvider(null));
        }

    }

    @Nested
    @DisplayName("when indicating browser support")
    class ProvidesBinaryForBrowserTest {

        @Test
        @DisplayName("should return true for Edge")
        void shouldReturnTrueForEdgeBrowser() {
            assertThat(binaryProvider.providesBinaryForBrowser(Browser.EDGE)).isTrue();
        }

        @ParameterizedTest(name = "[{index}] browser={0}")
        @EnumSource(Browser.class)
        @DisplayName("should return false for every browser except Edge")
        void shouldReturnFalseForEveryBrowserExceptEdge(final Browser browser) {
            assumeFalse(Browser.EDGE == browser);

            assertThat(binaryProvider.providesBinaryForBrowser(browser)).isFalse();
        }

    }

    @Nested
    @DisplayName("when determining the latest binary version")
    class GetLatestBinaryVersionTest {

        @Test
        @DisplayName("should return the latest available version")
        void shouldReturnLatestVersion() throws IOException {
            //noinspection unchecked
            given(httpClientMock.execute(argThat(isHttpGetWithUrl(TEST_BASE_URL)), any(ResponseHandler.class)))
                    .willReturn(Arrays.asList(
                            new MicrosoftWebDriverRelease("1.1", null),
                            new MicrosoftWebDriverRelease("1.2", null)
                    ));

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X86))
                    .hasValue("1.2");

            assertThat(binaryProvider.getLatestBinaryVersion(Os.WINDOWS, Architecture.X64))
                    .hasValue("1.2");
        }

        @ParameterizedTest(name = "[{index}] os={0}")
        @EnumSource(Os.class)
        @DisplayName("should return an empty Optional for every OS except Windows")
        void shouldReturnEmptyOptionalForEveryOsExceptWindows(final Os os) throws IOException {
            assumeFalse(Os.WINDOWS == os);

            assertThat(binaryProvider.getLatestBinaryVersion(os, Architecture.X86)).isNotPresent();

            assertThat(binaryProvider.getLatestBinaryVersion(os, Architecture.X64)).isNotPresent();
        }

    }

    @Nested
    @DisplayName("when downloading binaries")
    class DownloadTest {

        @ParameterizedTest(name = "[{index}] os={0}")
        @EnumSource(Os.class)
        @DisplayName("should throw an exception for every OS except Windows")
        @SuppressWarnings("Duplicates")
        void shouldThrowExceptionForEveryOsExceptWindows(final Os os) {
            assumeFalse(Os.WINDOWS == os);

            final String version = "doesNotMatter";

            final Path mockedPath = mock(Path.class);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryProvider.download(version, os, Architecture.X86, mockedPath));

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryProvider.download(version, os, Architecture.X64, mockedPath));

        }

        @Test
        @DisplayName("should throw an exception when desired version does not exist")
        void shouldThrowExceptionWhenDesiredVersionDoesNotExist() throws IOException {
            //noinspection unchecked
            given(httpClientMock.execute(argThat(isHttpGetWithUrl(TEST_BASE_URL)), any(ResponseHandler.class)))
                    .willReturn(emptyList());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> binaryProvider.download("doesNotMatter", Os.WINDOWS, Architecture.X86, mock(Path.class)));
        }

    }

    private static ArgumentMatcher<HttpGet> isHttpGetWithUrl(final String url) {
        return argument -> argument.getURI().toString().equals(url);
    }

}