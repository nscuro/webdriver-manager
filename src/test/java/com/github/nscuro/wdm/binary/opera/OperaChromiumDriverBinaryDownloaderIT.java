package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("The OperaChromiumDriver binary downloader")
public class OperaChromiumDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private OperaChromiumDriverBinaryDownloader binaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        binaryDownloader = new OperaChromiumDriverBinaryDownloader(GitHubReleasesService.create(getHttpClient()));
    }

    @Test
    @DisplayName("should be able to download the latest binary for Linux 64bit")
    void testDownloadLatestForLinux64() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.LINUX, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should throw an exception when trying to download latest binary for Linux 32bit")
    void testDownloadLatestForLinux32() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> downloadedFile = binaryDownloader.downloadLatest(Os.LINUX, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH));
    }

    @Test
    @DisplayName("should be able to download the latest binary for Mac 64bit")
    void testDownloadLatestForMac64() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.MACOS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should throw an exception when trying to download latest binary for MacOS 32bit")
    void testDownloadLatestForMac32() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = binaryDownloader.downloadLatest(Os.MACOS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH));
    }

    @Test
    @DisplayName("should be able to download the latest binary for Windows 64bit")
    void testDownloadLatestForWin64() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @DisplayName("should be able to download the latest binary for Windows 32bit")
    void testDownloadLatestForWin32() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

}
