package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
    @Override
    protected void testDownloadSpecificVersion() throws IOException {
        downloadedFile = binaryDownloader.download("2.32", Os.LINUX, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @Override
    protected void testDownloadLatest() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.WINDOWS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }


}
