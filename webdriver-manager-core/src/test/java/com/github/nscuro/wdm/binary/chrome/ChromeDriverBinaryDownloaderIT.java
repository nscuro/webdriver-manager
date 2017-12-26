package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("The ChromeDriver binary downloader")
class ChromeDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private ChromeDriverBinaryDownloader chromeDriverBinaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        chromeDriverBinaryDownloader = new ChromeDriverBinaryDownloader(getHttpClient());
    }

    @Test
    void testGetLatestVersion() throws IOException {
        assertThat(chromeDriverBinaryDownloader.getLatestVersion())
                .as("should return a valid version string")
                .matches("[0-9]+.[0-9]+");
    }

    @Test
    @DisplayName("should be able to download the latest binary for Windows 64bit")
    void testDownloadLatestForWindows64() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Windows 32bit")
    void testDownloadLatestForWindows32() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Linux 64bit")
    void testDownloadLatestForLinux64() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.LINUX, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Linux 32bit")
    void testDownloadLatestForLinux32() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.LINUX, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for MacOS 64bit")
    void testDownloadLatestForMacOs64() throws IOException {
        downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.MACOS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should throw an exception when trying to download latest binary for MacOS 32bit")
    void testDownloadLatestForMacOs32() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = chromeDriverBinaryDownloader.downloadLatest(Os.MACOS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH));
    }

}