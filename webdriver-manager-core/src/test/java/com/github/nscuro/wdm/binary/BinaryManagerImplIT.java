package com.github.nscuro.wdm.binary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.binary.chrome.ChromeDriverBinaryDownloader;
import com.github.nscuro.wdm.binary.firefox.GeckoDriverBinaryDownloader;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryManagerImplIT {

    private static final String gitHubUserName = System.getenv("WDM_GH_USER");

    private static final String gitHubToken = System.getenv("WDM_GH_TOKEN");

    private BinaryManager binaryManager;

    private File downloadedFile;

    @BeforeEach
    void beforeEach() {
        final HttpClient httpClient = HttpClients.custom().disableCookieManagement().build();

        final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final GitHubReleasesService gitHubReleasesService = GitHubReleasesService
                .createWithToken(httpClient, objectMapper, gitHubUserName, gitHubToken);

        final BinaryDownloader chromeDriverBinaryDownloader = new ChromeDriverBinaryDownloader(httpClient);

        final BinaryDownloader geckoDriverBinaryDownloader = new GeckoDriverBinaryDownloader(httpClient, gitHubReleasesService);

        binaryManager = new BinaryManagerImpl(new HashSet<>(Arrays.asList(chromeDriverBinaryDownloader, geckoDriverBinaryDownloader)));
    }

    @Nested
    class GetBinaryTest {

        @ParameterizedTest
        @EnumSource(Browser.class)
        void testGetLatestBinaryForCurrentOsAndArchitecture(final Browser browser) throws IOException {
            downloadedFile = binaryManager.getBinary(browser);

            assertThat(downloadedFile).exists();
            assertThat(downloadedFile.canExecute())
                    .as("The downloaded binary must be executable")
                    .isTrue();
        }

    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(downloadedFile).ifPresent(File::delete);
    }

}