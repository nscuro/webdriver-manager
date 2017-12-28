package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryDownloader;
import com.github.nscuro.wdm.binary.BinaryExtractor;
import com.github.nscuro.wdm.binary.github.GitHubRelease;
import com.github.nscuro.wdm.binary.github.GitHubReleaseAsset;
import com.github.nscuro.wdm.binary.github.GitHubReleasesService;
import com.github.nscuro.wdm.binary.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_GZIP;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.util.Objects.requireNonNull;

public final class GeckoDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeckoDriverBinaryDownloader.class);

    private static final String GITHUB_REPOSITORY_OWNER = "mozilla";

    private static final String GITHUB_REPOSITORY_NAME = "geckodriver";

    private final GitHubReleasesService releasesService;

    public GeckoDriverBinaryDownloader(final GitHubReleasesService releasesService) {
        this.releasesService = requireNonNull(releasesService, "No GitHubReleasesService provided");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.FIREFOX == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final GeckoDriverPlatform driverPlatform = GeckoDriverPlatform.valueOf(os, architecture);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.FIREFOX, version, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("GeckoDriver {} for {} was already downloaded", version, driverPlatform);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading GeckoDriver {} for {}", version, driverPlatform);
        }

        final GitHubRelease specificRelease = releasesService
                .getReleaseByTagName(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, version)
                .orElseThrow(NoSuchElementException::new);

        final GitHubReleaseAsset releaseAsset = getReleaseAssetForPlatform(specificRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        return unarchiveBinary(releasesService.downloadAsset(releaseAsset), releaseAsset.getContentType(), destinationFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final GeckoDriverPlatform driverPlatform = GeckoDriverPlatform.valueOf(os, architecture);

        final GitHubRelease latestRelease = releasesService
                .getLatestRelease(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME)
                .orElseThrow(NoSuchElementException::new);

        final String latestVersion = latestRelease.getTagName();

        LOGGER.info("Latest GeckoDriver version is {}", latestVersion);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.FIREFOX, latestVersion, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("GeckoDriver {} for {} was already downloaded", latestVersion, driverPlatform);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading GeckoDriver {} for {}", latestVersion, driverPlatform);
        }

        final GitHubReleaseAsset releaseAsset = getReleaseAssetForPlatform(latestRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        return unarchiveBinary(releasesService.downloadAsset(releaseAsset), releaseAsset.getContentType(), destinationFilePath);
    }

    @Nonnull
    Optional<GitHubReleaseAsset> getReleaseAssetForPlatform(final GitHubRelease release, final GeckoDriverPlatform platform) {
        return release.getAssets().stream()
                .filter(asset -> asset.getName().toLowerCase().contains(platform.name().toLowerCase()))
                .findAny();
    }

    /**
     * Unarchive a given archived binary file.
     *
     * @param archivedBinaryFile  The {@link File} of the archived binary
     * @param mimeType            The MIME-type of the given file. This is required in order to determine
     *                            which decompressing-strategy to use.
     * @param destinationFilePath {@link Path} where the binary should be unarchived to
     * @return A {@link File} handle of the unarchived binary
     * @throws IOException When unarchiving failed
     */
    @Nonnull
    File unarchiveBinary(final File archivedBinaryFile, final String mimeType, final Path destinationFilePath) throws IOException {
        try (final BinaryExtractor binaryExtractor = BinaryExtractor.fromArchiveFile(archivedBinaryFile)) {
            switch (mimeType) {
                case APPLICATION_GZIP:
                    return binaryExtractor.unTarGz(destinationFilePath,
                            entryIsFile().and(entryNameStartsWithIgnoringCase("geckodriver")));
                case APPLICATION_ZIP:
                    return binaryExtractor.unZip(destinationFilePath,
                            entryIsFile().and(entryNameStartsWithIgnoringCase("geckodriver")));
                default:
                    throw new IllegalArgumentException("Unable to unarchive file with MIME-type " + mimeType);
            }
        }
    }

}
