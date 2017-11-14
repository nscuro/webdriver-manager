package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChromeDriverBinaryDownloaderIT {

    private static final Path DOWNLOAD_DESTINATION_DIR_PATH = Paths.get(System.getProperty("java.io.tmpdir"));

    private ChromeDriverBinaryDownloader chromeDriverBinaryDownloader;

    private File downloadedFile;

    @BeforeEach
    void beforeEach() {
        chromeDriverBinaryDownloader = new ChromeDriverBinaryDownloader(HttpClients.createSystem());
    }

    @Test
    void testGetLatestVersion() throws IOException {
        assertThat(chromeDriverBinaryDownloader.getLatestVersion())
                .as("should return a valid version string")
                .matches("[0-9]+.[0-9]+");
    }

    @Test
    void testDownloadSpecificVersionForCurrentOsAndArchitecture() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.download("2.31", Os.getCurrent(), Architecture.getCurrent(), DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    void testDownloadLatestVersionForCurrentOsAndArchitecture() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.getCurrent(), Architecture.getCurrent(), DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @AfterEach
    void afterEach() {
        Optional.ofNullable(downloadedFile).ifPresent(File::delete);
    }

}