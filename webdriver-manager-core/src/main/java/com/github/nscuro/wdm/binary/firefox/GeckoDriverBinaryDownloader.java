package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryDownloader;
import com.github.nscuro.wdm.binary.CompressionUtils;
import com.github.nscuro.wdm.binary.FileUtils;
import com.github.nscuro.wdm.binary.github.GitHubReleaseAsset;
import com.github.nscuro.wdm.binary.github.GitHubRelease;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class GeckoDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeckoDriverBinaryDownloader.class);

    private static final String REPOSITORY_OWNER = "mozilla";

    private static final String REPOSITORY_NAME = "geckodriver";

    private final HttpClient httpClient;

    private final GitHubReleasesService releasesService;

    GeckoDriverBinaryDownloader(final HttpClient httpClient,
                                final GitHubReleasesService releasesService) {
        this.httpClient = httpClient;
        this.releasesService = releasesService;
    }

    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.FIREFOX.equals(browser);
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final GeckoDriverPlatform driverPlatform = GeckoDriverPlatform.from(os, architecture);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.FIREFOX, version, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.info("GeckoDriver {} for {} was already downloaded", version, driverPlatform);

            return destinationFilePath.toFile();
        } else {
            LOGGER.info("Downloading GeckoDriver {} for {}", version, driverPlatform);
        }

        final GitHubRelease specificRelease = releasesService
                .getReleaseByTagName(REPOSITORY_OWNER, REPOSITORY_NAME, version)
                .orElseThrow(NoSuchElementException::new);

        final GitHubReleaseAsset releaseAsset = getReleaseAssetForPlatform(specificRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        final String downloadUrl = releaseAsset.getBrowserDownloadUrl();
        final String contentType = releaseAsset.getContentType();

        final byte[] archivedBinary = downloadArchivedBinary(downloadUrl, contentType);

        return unarchiveBinary(archivedBinary, contentType, destinationFilePath)
                .orElseThrow(IllegalStateException::new);
    }

    @Nonnull
    @Override
    public File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final GeckoDriverPlatform driverPlatform = GeckoDriverPlatform.from(os, architecture);

        final GitHubRelease latestRelease = releasesService
                .getLatestRelease(REPOSITORY_OWNER, REPOSITORY_NAME)
                .orElseThrow(NoSuchElementException::new);

        LOGGER.info("Latest GeckoDriver version is {}", latestRelease.getTagName());

        final String downloadUrl = getReleaseAssetForPlatform(latestRelease, driverPlatform)
                .map(GitHubReleaseAsset::getBrowserDownloadUrl)
                .orElseThrow(NoSuchElementException::new);

        return null;
    }

    @Nonnull
    Optional<GitHubReleaseAsset> getReleaseAssetForPlatform(final GitHubRelease release, final GeckoDriverPlatform platform) {
        return release.getAssets().stream()
                .filter(asset -> asset.getName().toLowerCase().contains(platform.name().toLowerCase()))
                .findAny();
    }

    private byte[] downloadArchivedBinary(final String downloadUrl, final String contentType) throws IOException {
        final HttpGet request = new HttpGet(downloadUrl);
        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);

        return httpClient.execute(request, httpResponse -> {
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    if (httpResponse.getEntity() == null) {
                        throw new IllegalStateException();
                    } else {
                        return EntityUtils.toByteArray(httpResponse.getEntity());
                    }
                default:
                    throw new IllegalStateException();
            }
        });
    }

    private Optional<File> unarchiveBinary(final byte[] archivedBinary, final String contentType, final Path destinationFilePath) throws IOException {
        switch (contentType) {
            case "application/zip":
                return CompressionUtils.unzipFile(archivedBinary, destinationFilePath,
                        zipEntry -> !zipEntry.isDirectory() && zipEntry.getName().toLowerCase().startsWith("geckodriver"));
            default:
                throw new IllegalStateException();
        }
    }

}
