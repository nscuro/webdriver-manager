package com.github.nscuro.wdm.binary.edge;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EdgeDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private EdgeDriverBinaryDownloader binaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        binaryDownloader = new EdgeDriverBinaryDownloader(getHttpClient());
    }

    @Test
    void testDownloadLatestForWindows64() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    void testDownloadLatestForWindows32() throws IOException {
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