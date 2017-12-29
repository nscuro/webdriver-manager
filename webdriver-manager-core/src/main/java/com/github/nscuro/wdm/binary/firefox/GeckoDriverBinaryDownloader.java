package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
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

import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_GZIP;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.util.Objects.requireNonNull;

public final class GeckoDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeckoDriverBinaryDownloader.class);

    private static final String BINARY_NAME = "geckodriver";

    private static final String GITHUB_REPOSITORY_OWNER = "mozilla";

    private static final String GITHUB_REPOSITORY_NAME = "geckodriver";

    private final GitHubReleasesService gitHubReleasesService;

    public GeckoDriverBinaryDownloader(final GitHubReleasesService gitHubReleasesService) {
        this.gitHubReleasesService = requireNonNull(gitHubReleasesService, "No GitHubReleasesService provided");
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
        final Platform driverPlatform = GeckoDriverPlatform.valueOf(os, architecture);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.FIREFOX, version, os, architecture, destinationDirPath);

        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("GeckoDriver {} for {} was already downloaded", version, driverPlatform);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading GeckoDriver {} for {}", version, driverPlatform);
        }

        final GitHubRelease specificRelease = gitHubReleasesService
                .getReleaseByTagName(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, version)
                .orElseThrow(NoSuchElementException::new);

        final GitHubReleaseAsset releaseAsset = gitHubReleasesService
                .getReleaseAssetForPlatform(specificRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        return unarchiveBinary(gitHubReleasesService.downloadAsset(releaseAsset), releaseAsset.getContentType(), destinationFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final Platform driverPlatform = GeckoDriverPlatform.valueOf(os, architecture);

        final GitHubRelease latestRelease = gitHubReleasesService
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

        final GitHubReleaseAsset releaseAsset = gitHubReleasesService
                .getReleaseAssetForPlatform(latestRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        return unarchiveBinary(gitHubReleasesService.downloadAsset(releaseAsset), releaseAsset.getContentType(), destinationFilePath);
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
    private File unarchiveBinary(final File archivedBinaryFile, final String mimeType, final Path destinationFilePath) throws IOException {
        try (final BinaryExtractor binaryExtractor = BinaryExtractor.fromArchiveFile(archivedBinaryFile)) {
            switch (mimeType) {
                case APPLICATION_GZIP:
                    return binaryExtractor.unTarGz(destinationFilePath,
                            entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
                case APPLICATION_ZIP:
                    return binaryExtractor.unZip(destinationFilePath,
                            entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
                default:
                    throw new IllegalArgumentException("Unable to unarchive file with MIME-type " + mimeType);
            }
        }
    }

}
