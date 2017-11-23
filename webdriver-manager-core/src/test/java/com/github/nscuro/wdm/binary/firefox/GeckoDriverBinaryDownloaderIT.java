package com.github.nscuro.wdm.binary.firefox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("The GeckoDriver binary downloader")
class GeckoDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private GeckoDriverBinaryDownloader geckoDriverBinaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        final GitHubReleasesService releasesService = GitHubReleasesService.create(getHttpClient(), new ObjectMapper());

        geckoDriverBinaryDownloader = new GeckoDriverBinaryDownloader(getHttpClient(), releasesService);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Windows 64bit")
    void testDownloadLatestForWindows64() throws IOException {
        downloadedFile = geckoDriverBinaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Windows 32bit")
    void testDownloadLatestForWindows32() throws IOException {
        downloadedFile = geckoDriverBinaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Linux 64bit")
    void testDownloadLatestForLinux64() throws IOException {
        downloadedFile = geckoDriverBinaryDownloader.downloadLatest(Os.LINUX, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Linux 32bit")
    void testDownloadLatestForLinux32() throws IOException {
        downloadedFile = geckoDriverBinaryDownloader.downloadLatest(Os.LINUX, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for MacOS 64bit")
    void testDownloadLatestForMacOs64() throws IOException {
        downloadedFile = geckoDriverBinaryDownloader.downloadLatest(Os.MACOS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should throw an exception when trying to download latest binary for MacOS 32bit")
    void testDownloadLatestForMacOs32() throws IOException {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = geckoDriverBinaryDownloader.downloadLatest(Os.MACOS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH));
    }

}