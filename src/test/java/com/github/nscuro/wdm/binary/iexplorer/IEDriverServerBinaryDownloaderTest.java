package com.github.nscuro.wdm.binary.iexplorer;

import com.github.nscuro.wdm.Browser;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;

class IEDriverServerBinaryDownloaderTest {

    private HttpClient httpClient;

    private IEDriverServerBinaryDownloader binaryDownloader;

    @BeforeEach
    void beforeEach() {
        httpClient = mock(HttpClient.class);

        binaryDownloader = new IEDriverServerBinaryDownloader(httpClient);
    }

    @Nested
    class SupportsBrowserTest {

        @Test
        void shouldReturnTrueForInternetExplorer() {
            assertThat(binaryDownloader.supportsBrowser(Browser.INTERNET_EXPLORER)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(Browser.class)
        void shouldReturnFalseForAnyBrowserThatIsntInternetExplorer(final Browser browser) {
            assumeFalse(browser == Browser.INTERNET_EXPLORER);

            assertThat(binaryDownloader.supportsBrowser(browser)).isFalse();
        }

    }

}