package com.github.nscuro.wdm.binary.opera;

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
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link BinaryProvider} for Opera's OperaChromiumDriver.
 *
 * @see <a href="https://github.com/operasoftware/operachromiumdriver">OperaChromiumDriver project page</a>
 * @since 0.2.0
 */
public final class OperaChromiumDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperaChromiumDriverBinaryProvider.class);

    private static final String BINARY_NAME = "operadriver";

    private final GitHubReleasesService gitHubReleasesService;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public OperaChromiumDriverBinaryProvider(final HttpClient httpClient) {
        this(GitHubReleasesService
                        .create(requireNonNull(httpClient, "no HttpClient provided"), "operasoftware", "operachromiumdriver"),
                new BinaryExtractorFactory());
    }

    OperaChromiumDriverBinaryProvider(final GitHubReleasesService gitHubReleasesService,
                                      final BinaryExtractorFactory binaryExtractorFactory) {
        this.gitHubReleasesService = gitHubReleasesService;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.OPERA == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final Optional<OperaChromiumDriverPlatform> platform = OperaChromiumDriverPlatform.valueOf(os, architecture);

        if (!platform.isPresent()) {
            LOGGER.warn("OperaChromiumDriver is not supported on {} {}", os, architecture);
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
        final OperaChromiumDriverPlatform platform = OperaChromiumDriverPlatform.valueOf(os, architecture)
                .orElseThrow(() -> new UnsupportedOperationException(
                        format("OperaChromiumDriver is not supported on %s %s", os, architecture)));

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

    @Nonnull
    private String normalizeTagName(final String tagName) {
        return requireNonNull(tagName).replace("v.", "").replace("v", "");
    }

}
