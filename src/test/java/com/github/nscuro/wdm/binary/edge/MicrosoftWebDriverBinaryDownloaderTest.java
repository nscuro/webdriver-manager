package com.github.nscuro.wdm.binary.edge;

import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.apache.http.client.HttpClient;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

class MicrosoftWebDriverBinaryDownloaderTest {

    private HttpClient httpClient;

    private MicrosoftWebDriverBinaryDownloader binaryDownloader;

    @BeforeEach
    void beforeEach() {
        httpClient = mock(HttpClient.class);

        binaryDownloader = new MicrosoftWebDriverBinaryDownloader(httpClient);
    }

    @Test
    void shouldOnlySupportMicrosoftEdge() {
        assertThat(binaryDownloader.supportsBrowser(Browser.EDGE)).isTrue();

        final SoftAssertions assertion = new SoftAssertions();
        Stream.of(Browser.values())
                .filter(browser -> browser != Browser.EDGE)
                .forEach(browser -> assertion
                        .assertThat(binaryDownloader.supportsBrowser(browser))
                        .as("should not support any browser except Microsoft Edge")
                        .isFalse());

        assertion.assertAll();
    }

    @Nested
    class RequireWindowsOsTest {

        @Test
        void shouldDoNothingWhenOsIsWindows() {
            binaryDownloader.requireWindowsOs(Os.WINDOWS);
        }

        @Test
        void shouldThrowExceptionWhenOsIsNotWindows() {
            final SoftAssertions assertions = new SoftAssertions();

            Stream.of(Os.values())
                    .filter(os -> os != Os.WINDOWS)
                    .forEach(os -> assertThatExceptionOfType(IllegalArgumentException.class)
                            .isThrownBy(() -> binaryDownloader.requireWindowsOs(os)));

            assertions.assertAll();
        }

    }

}