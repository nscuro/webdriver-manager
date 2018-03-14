package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.VersionComparator;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.github.GitHubRelease;
import com.github.nscuro.wdm.binary.util.github.GitHubReleaseAsset;
import com.github.nscuro.wdm.binary.util.github.GitHubReleasesService;
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

public final class OperaChromiumDriverBinaryProvider implements BinaryProvider {

    private static final String BINARY_NAME = "operadriver";

    private final GitHubReleasesService gitHubReleasesService;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public OperaChromiumDriverBinaryProvider(final HttpClient httpClient) {
        this(GitHubReleasesService.create(httpClient, "operasoftware", "operachromiumdriver"),
                new BinaryExtractorFactory());
    }

    OperaChromiumDriverBinaryProvider(final GitHubReleasesService gitHubReleasesService,
                                      final BinaryExtractorFactory binaryExtractorFactory) {
        this.gitHubReleasesService = gitHubReleasesService;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.OPERA == browser;
    }

    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final Platform platform = OperaChromiumDriverPlatform.valueOf(os, architecture);

        return gitHubReleasesService
                .getAllReleases()
                .stream()
                .filter(release -> release.hasAssetForPlatform(platform))
                .map(GitHubRelease::getTagName)
                .map(this::normalizeTagName)
                .max(new VersionComparator());
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        final Platform platform = OperaChromiumDriverPlatform.valueOf(os, architecture);

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
