package com.github.nscuro.wdm.binary.iexplorer;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class IEDriverServerBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private IEDriverServerBinaryDownloader binaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        binaryDownloader = new IEDriverServerBinaryDownloader(getHttpClient());
    }

    @Test
    @Override
    protected void testDownloadSpecificVersion() throws IOException {
        downloadedFile = binaryDownloader.download("3.8", Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @Override
    protected void testDownloadLatest() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X86, DOWNLOAD_DESTINATION_DIR_PATH);
    }

}