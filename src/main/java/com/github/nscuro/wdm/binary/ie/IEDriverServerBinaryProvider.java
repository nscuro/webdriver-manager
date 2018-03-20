package com.github.nscuro.wdm.binary.ie;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.VersionComparator;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectoryService;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
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
 * A {@link BinaryProvider} for Microsoft's IEDriverServer.
 *
 * @since 0.2.0
 */
public final class IEDriverServerBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(IEDriverServerBinaryProvider.class);

    private static final String BINARY_NAME = "IEDriverServer";

    private final GoogleCloudStorageDirectoryService cloudStorageDirectory;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public IEDriverServerBinaryProvider(final HttpClient httpClient) {
        this(GoogleCloudStorageDirectoryService
                        .create(requireNonNull(httpClient, "no HttpClient provided"),
                                "https://selenium-release.storage.googleapis.com/"),
                new BinaryExtractorFactory());
    }

    IEDriverServerBinaryProvider(final GoogleCloudStorageDirectoryService cloudStorageDirectory,
                                 final BinaryExtractorFactory binaryExtractorFactory) {
        this.cloudStorageDirectory = cloudStorageDirectory;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.INTERNET_EXPLORER == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        if (os != Os.WINDOWS) {
            LOGGER.warn("IEDriverServer is only supported on Windows systems");
            return Optional.empty();
        }

        return cloudStorageDirectory
                .getEntries()
                .stream()
                .map(this::toIEDriverServerRelease)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(release -> release.getArchitecture() == architecture)
                .map(IEDriverServerRelease::getVersion)
                .max(new VersionComparator());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        if (os != Os.WINDOWS) {
            throw new UnsupportedOperationException("IEDriverServer is only supported on Windows systems");
        }

        final IEDriverServerRelease matchingRelease = cloudStorageDirectory
                .getEntries()
                .stream()
                .map(this::toIEDriverServerRelease)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(release -> release.getArchitecture() == architecture)
                .filter(release -> release.getVersion().equals(version))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        format("No IEDriverServer binary available for %s %s in version %s", os, architecture, version)));

        return binaryExtractorFactory
                .getBinaryExtractorForArchiveFile(cloudStorageDirectory.downloadFile(matchingRelease))
                .extractBinary(binaryDestinationPath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Browser.INTERNET_EXPLORER);
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

        return ((BinaryProvider) otherObject).providesBinaryForBrowser(Browser.INTERNET_EXPLORER);
    }

    @Nonnull
    private Optional<IEDriverServerRelease> toIEDriverServerRelease(final GoogleCloudStorageEntry entry) {
        if (!entry.getKey().contains(BINARY_NAME)) {
            return Optional.empty();
        }

        final String version = entry.getKey().split("/")[0];

        final Architecture architecture;
        if (entry.getKey().toLowerCase().contains("x64")) {
            architecture = Architecture.X64;
        } else if (entry.getKey().toLowerCase().contains("win32")) {
            architecture = Architecture.X86;
        } else {
            LOGGER.warn("Unable to detect architecture from \"{}\"", entry.getKey());
            return Optional.empty();
        }

        return Optional.of(new IEDriverServerRelease(entry.getKey(), entry.getUrl(), version, architecture));
    }

}
