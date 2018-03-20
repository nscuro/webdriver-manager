package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.VersionComparator;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.github.GitHubRelease;
import com.github.nscuro.wdm.binary.util.github.GitHubReleaseAsset;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesService;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link BinaryProvider} for Mozilla's GeckoDriver.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Mozilla/QA/Marionette/WebDriver">Mozilla WebDriver homepage</a>
 * @see <a href="https://github.com/mozilla/geckodriver">GeckoDriver project page</a>
 * @since 0.2.0
 */
public final class GeckoDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeckoDriverBinaryProvider.class);

    private static final String BINARY_NAME = "geckodriver";

    private final GitHubReleasesService gitHubReleasesService;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public GeckoDriverBinaryProvider(final HttpClient httpClient) {
        this(GitHubReleasesService
                        .create(requireNonNull(httpClient, "no HttpClient provided"), "mozilla", "geckodriver"),
                new BinaryExtractorFactory());
    }

    GeckoDriverBinaryProvider(final GitHubReleasesService gitHubReleasesService,
                              final BinaryExtractorFactory binaryExtractorFactory) {
        this.gitHubReleasesService = gitHubReleasesService;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.FIREFOX == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final Optional<GeckoDriverPlatform> platform = GeckoDriverPlatform.valueOf(os, architecture);

        if (!platform.isPresent()) {
            LOGGER.warn("GeckoDriver is not supported on {} {}", os, architecture);
            return Optional.empty();
        }

        return gitHubReleasesService
                .getAllReleases()
                .stream()
                .filter(release -> release.hasAssetForPlatform(platform.get()))
                .map(GitHubRelease::getTagName)
                .map(this::normalizeTagName)
                .max(new VersionComparator());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        final GeckoDriverPlatform platform = GeckoDriverPlatform.valueOf(os, architecture)
                .orElseThrow(() -> new UnsupportedOperationException(
                        format("GeckoDriver is not supported on %s %s", os, architecture)));

        final GitHubReleaseAsset matchingAsset = gitHubReleasesService
                .getAllReleases()
                .stream()
                .filter(release -> normalizeTagName(release.getTagName()).equals(version))
                .flatMap(release -> release.getAssets().stream())
                .filter(asset -> asset.isAssetForPlatform(platform))
                .findAny()
                .orElseThrow(NoSuchElementException::new);

        return binaryExtractorFactory
                .getBinaryExtractorForArchiveFile(gitHubReleasesService.downloadAsset(matchingAsset))
                .extractBinary(binaryDestinationPath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Browser.FIREFOX);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (otherObject == null) {
            return false;
        } else if (otherObject == this) {
            return true;
        } else if (!BinaryProvider.class.isInstance(otherObject)) {
            return false;
        }

        return ((BinaryProvider) otherObject).providesBinaryForBrowser(Browser.FIREFOX);
    }

    @Nonnull
    private String normalizeTagName(final String tagName) {
        return requireNonNull(tagName).replace("v", "");
    }

}
