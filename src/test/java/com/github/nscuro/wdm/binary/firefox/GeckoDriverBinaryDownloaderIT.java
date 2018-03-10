package com.github.nscuro.wdm.binary.firefox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.AbstractBinaryDownloaderIT;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("The GeckoDriver binary downloader")
class GeckoDriverBinaryDownloaderIT extends AbstractBinaryDownloaderIT {

    private GeckoDriverBinaryDownloader binaryDownloader;

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        final GitHubReleasesService releasesService = GitHubReleasesService.create(getHttpClient(), new ObjectMapper());

        binaryDownloader = new GeckoDriverBinaryDownloader(releasesService);
    }

    @Test
    @Override
    protected void testDownloadSpecificVersion() throws IOException {
        downloadedFile = binaryDownloader.download("v0.19.0", Os.LINUX, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

    @Test
    @Override
    protected void testDownloadLatest() throws IOException {
        downloadedFile = binaryDownloader.downloadLatest(Os.MACOS, Architecture.X64, DOWNLOAD_DESTINATION_DIR_PATH);
    }

}