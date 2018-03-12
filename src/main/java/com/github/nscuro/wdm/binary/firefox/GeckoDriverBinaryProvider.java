package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.github.GitHubRelease;
import com.github.nscuro.wdm.binary.util.github.GitHubReleaseAsset;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesServiceV2;
import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static java.util.Objects.requireNonNull;

/**
 * @since 0.1.5
 */
public final class GeckoDriverBinaryProvider implements BinaryProvider {

    private static final String BINARY_NAME = "geckodriver";

    private final GitHubReleasesServiceV2 gitHubReleasesService;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public GeckoDriverBinaryProvider(final HttpClient httpClient) {
        this(GitHubReleasesServiceV2.create(httpClient, "mozilla", "geckodriver"),
                new BinaryExtractorFactory());
    }

    GeckoDriverBinaryProvider(final GitHubReleasesServiceV2 gitHubReleasesService,
                              final BinaryExtractorFactory binaryExtractorFactory) {
        this.gitHubReleasesService = gitHubReleasesService;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.FIREFOX == browser;
    }

    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final GeckoDriverPlatform platform = GeckoDriverPlatform.valueOf(os, architecture);

        return gitHubReleasesService
                .getAllReleases()
                .stream()
                .filter(release -> release.hasAssetForPlatform(platform))
                .map(GitHubRelease::getTagName)
                .map(this::normalizeTagName)
                .max(Comparator.naturalOrder());
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        final GeckoDriverPlatform platform = GeckoDriverPlatform.valueOf(os, architecture);

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
        return requireNonNull(tagName).replace("v", "");
    }

}
