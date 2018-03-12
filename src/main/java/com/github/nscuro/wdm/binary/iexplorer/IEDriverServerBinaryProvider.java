package com.github.nscuro.wdm.binary.iexplorer;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static java.lang.String.format;

/**
 * @since 0.1.5
 */
public final class IEDriverServerBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(IEDriverServerBinaryProvider.class);

    private static final String DEFAULT_GCS_DIRECTORY_URL = "https://selenium-release.storage.googleapis.com/";

    private static final String BINARY_NAME = "IEDriverServer";

    private final GoogleCloudStorageDirectory cloudStorageDirectory;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public IEDriverServerBinaryProvider(final HttpClient httpClient) {
        this(new GoogleCloudStorageDirectory(httpClient, DEFAULT_GCS_DIRECTORY_URL), new BinaryExtractorFactory());
    }

    IEDriverServerBinaryProvider(final GoogleCloudStorageDirectory cloudStorageDirectory,
                                 final BinaryExtractorFactory binaryExtractorFactory) {
        this.cloudStorageDirectory = cloudStorageDirectory;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.INTERNET_EXPLORER == browser;
    }

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
                .max(Comparator.naturalOrder());
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        if (os != Os.WINDOWS) {
            throw new UnsupportedOperationException("IEDriverServer is only supported on Windows systems");
        }

        final String downloadUrl = cloudStorageDirectory
                .getEntries()
                .stream()
                .map(this::toIEDriverServerRelease)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(release -> release.getArchitecture() == architecture)
                .filter(release -> release.getVersion().equals(version))
                .findAny()
                .map(IEDriverServerRelease::getDownloadUrl)
                .orElseThrow(() -> new NoSuchElementException(
                        format("No IEDriverServer binary available for %s %s in version %s", os, architecture, version)));

        return binaryExtractorFactory
                .getBinaryExtractorForArchiveFile(cloudStorageDirectory.downloadFile(downloadUrl))
                .extractBinary(binaryDestinationPath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
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

        return Optional.of(new IEDriverServerRelease(version, architecture, entry.getUrl()));
    }

}
