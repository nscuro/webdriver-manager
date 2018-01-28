package com.github.nscuro.wdm.binary.iexplorer;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InternetExplorerDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private InternetExplorerDriverBinaryDownloader binaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        binaryDownloader = new InternetExplorerDriverBinaryDownloader(getHttpClient());
    }

    @Test
    void testDownloadSpecificVersion() throws IOException {
        downloadedFile = binaryDownloader.download("3.5", Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    void testDownloadLatestForWin64() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    void testDownloadLatestForWin32() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    void testDownloadLatestForLinux64() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = binaryDownloader.downloadLatest(Os.LINUX, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH));
    }

    @Test
    void testDownloadLatestForLinux32() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = binaryDownloader.downloadLatest(Os.LINUX, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH));
    }

    @Test
    void testDownloadLatestForMac64() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = binaryDownloader.downloadLatest(Os.MACOS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH));
    }

    @Test
    void testDownloadLatestForMac32() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> downloadedFile = binaryDownloader.downloadLatest(Os.MACOS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH));
    }

}