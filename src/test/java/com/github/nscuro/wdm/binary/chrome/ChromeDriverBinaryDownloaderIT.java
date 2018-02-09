package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The ChromeDriver binary downloader")
class ChromeDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private ChromeDriverBinaryDownloader binaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        binaryDownloader = new ChromeDriverBinaryDownloader(getHttpClient());
    }

    @Test
    void testGetLatestVersion() throws IOException {
        assertThat(binaryDownloader.getLatestVersion())
                .as("should return a valid version string")
                .matches("[0-9]+.[0-9]+");
    }

    @Test
    @Override
    protected void testDownloadSpecificVersion() throws IOException {
        downloadedFile = binaryDownloader.download("2.34", Os.MACOS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @Override
    protected void testDownloadLatest() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

}