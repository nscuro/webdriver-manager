package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import com.github.nscuro.wdm.binary.BinaryDownloader;
import com.github.nscuro.wdm.binary.BinaryExtractor;
import com.github.nscuro.wdm.binary.firefox.GeckoDriverBinaryDownloader;
import com.github.nscuro.wdm.binary.util.github.GitHubRelease;
import com.github.nscuro.wdm.binary.util.github.GitHubReleaseAsset;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesService;
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
import static java.util.Objects.requireNonNull;

/**
 * A {@link BinaryDownloader} for Opera's <a href="https://github.com/operasoftware/operachromiumdriver">operachromiumdriver</a>.
 */
public final class OperaChromiumDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeckoDriverBinaryDownloader.class);

    private static final String BINARY_NAME = "operadriver";

    private static final String GITHUB_REPOSITORY_OWNER = "operasoftware";

    private static final String GITHUB_REPOSITORY_NAME = "operachromiumdriver";

    private final GitHubReleasesService gitHubReleasesService;

    public OperaChromiumDriverBinaryDownloader(final GitHubReleasesService gitHubReleasesService) {
        this.gitHubReleasesService = requireNonNull(gitHubReleasesService, "No GitHubReleasesService provided");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.OPERA == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final Platform driverPlatform = OperaChromiumDriverPlatform.valueOf(os, architecture);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.OPERA, version, os, architecture, destinationDirPath);

        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("OperaChromiumDriver {} for {} was already downloaded", version, driverPlatform.getName());

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading OperaChromiumDriver {} for {}", version, driverPlatform.getName());
        }

        final GitHubRelease specificRelease = gitHubReleasesService
                .getReleaseByTagName(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, version)
                .orElseThrow(NoSuchElementException::new);

        final GitHubReleaseAsset releaseAsset = gitHubReleasesService
                .getReleaseAssetForPlatform(specificRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        return unarchiveBinary(gitHubReleasesService.downloadAsset(releaseAsset), destinationFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final Platform driverPlatform = OperaChromiumDriverPlatform.valueOf(os, architecture);

        final GitHubRelease latestRelease = gitHubReleasesService
                .getLatestRelease(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME)
                .orElseThrow(NoSuchElementException::new);

        final String latestVersion = latestRelease.getTagName();

        LOGGER.debug("Latest OperaChromiumDriver version is {}", latestVersion);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.OPERA, latestVersion, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("OperaChromiumDriver {} for {} was already downloaded", latestVersion, driverPlatform);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading OperaChromiumDriver {} for {}", latestVersion, driverPlatform);
        }

        final GitHubReleaseAsset releaseAsset = gitHubReleasesService
                .getReleaseAssetForPlatform(latestRelease, driverPlatform)
                .orElseThrow(NoSuchElementException::new);

        return unarchiveBinary(gitHubReleasesService.downloadAsset(releaseAsset), destinationFilePath);
    }

    @Nonnull
    private File unarchiveBinary(final File archivedBinaryFile, final Path destinationFilePath) throws IOException {
        return BinaryExtractor
                .fromArchiveFile(archivedBinaryFile)
                .unZip(destinationFilePath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

}
