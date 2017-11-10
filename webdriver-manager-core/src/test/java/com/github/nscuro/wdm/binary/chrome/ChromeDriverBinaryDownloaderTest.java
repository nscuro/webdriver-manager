package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Browser;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;

class ChromeDriverBinaryDownloaderTest {

    private ChromeDriverBinaryDownloader binaryDownloader;

    private HttpClient httpClient;

    @BeforeEach
    void beforeEach() {
        httpClient = mock(HttpClient.class);

        binaryDownloader = new ChromeDriverBinaryDownloader(httpClient);
    }

    @Test
    void testSupportsBrowser() {
        assertThat(binaryDownloader.supportsBrowser(Browser.CHROME))
                .isTrue();
    }

    @ParameterizedTest
    @EnumSource(Browser.class)
    void testSupportsBrowserForUnsupportedBrowsers(final Browser browser) {
        assumeFalse(Browser.CHROME.equals(browser));

        assertThat(binaryDownloader.supportsBrowser(browser))
                .isFalse();
    }

}