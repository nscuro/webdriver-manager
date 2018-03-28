package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.ie.IEDriverServerBinaryProvider;
import com.github.nscuro.wdm.binary.util.VersionComparator;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectoryService;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link BinaryProvider} for Google's ChromeDriver.
 *
 * @see <a href="https://sites.google.com/a/chromium.org/chromedriver/">ChromeDriver homepage</a>
 * @since 0.2.0
 */
public final class ChromeDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeDriverBinaryProvider.class);

    private static final String BINARY_NAME = "chromedriver";

    private final HttpClient httpClient;

    private final GoogleCloudStorageDirectoryService cloudStorageDirectory;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public ChromeDriverBinaryProvider(final HttpClient httpClient) {
        this(requireNonNull(httpClient, "no HttpClient provided"),
                GoogleCloudStorageDirectoryService.create(httpClient, "https://chromedriver.storage.googleapis.com/"),
                new BinaryExtractorFactory());
    }

    ChromeDriverBinaryProvider(final HttpClient httpClient,
                               final GoogleCloudStorageDirectoryService cloudStorageDirectory,
                               final BinaryExtractorFactory binaryExtractorFactory) {
        this.httpClient = httpClient;
        this.cloudStorageDirectory = cloudStorageDirectory;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false} for every {@link Browser} except {@link Browser#CHROME},
     *         in which case {@code true} is returned
     */
    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.CHROME == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final Optional<ChromeDriverPlatform> platform = ChromeDriverPlatform.valueOf(os, architecture);

        if (!platform.isPresent()) {
            LOGGER.warn("ChromeDriver is not supported on {} {}", os, architecture);
            return Optional.empty();
        }

        final List<GoogleCloudStorageEntry> cloudStorageEntries = cloudStorageDirectory.getEntries();

        final String latestVersion = getLatestReleaseVersion(cloudStorageEntries);

        return cloudStorageDirectory
                .getEntries()
                .stream()
                .map(GoogleCloudStorageEntry::getKey)
                .map(String::toLowerCase)
                .filter(key -> key.contains(platform.get().getName()))
                .map(key -> key.split("/")[0])
                // For whatever reason there are versions higher than LATEST_RELEASE in the directory
                // that are older than any of those equal to or lower than LATEST_RELEASE...
                .filter(version -> new VersionComparator().compare(version, latestVersion) <= 0)
                .max(new VersionComparator());
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException When the requested {@link Os} / {@link Architecture} combination is not supported
     * @throws NoSuchElementException        When no binary for the requested criteria was found
     */
    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        final ChromeDriverPlatform platform = ChromeDriverPlatform.valueOf(os, architecture)
                .orElseThrow(() -> new UnsupportedOperationException(
                        format("ChromeDriver is not supported on %s %s", os, architecture)));

        final GoogleCloudStorageEntry binaryFileEntry = cloudStorageDirectory
                .getEntries()
                .stream()
                .filter(entry -> entry.getKey().contains(version))
                .filter(entry -> entry.getKey().contains(platform.getName()))
                .findAny()
                .orElseThrow(NoSuchElementException::new);

        return binaryExtractorFactory
                .getBinaryExtractorForArchiveFile(cloudStorageDirectory.downloadFile(binaryFileEntry))
                .extractBinary(binaryDestinationPath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Browser.CHROME);
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

        return ((BinaryProvider) otherObject).providesBinaryForBrowser(Browser.CHROME);
    }

    /**
     * Determine the latest available release version.
     * <p>
     * This is not an as trivial task as for {@link IEDriverServerBinaryProvider}, because the highest
     * available version for ChromeDriver is not the latest.
     * <p>
     * At the time of writing this, there are versions {@code 2.4, 2.5, 2.6, 2.7, 2.8, 2.9} available,
     * yet {@link 2.37} is the latest. Google is kind enough here to point us to the <b>actual</b> latest
     * version through a LATEST_RELEASE file.
     *
     * @param directoryEntries Entries of the {@link GoogleCloudStorageDirectoryService}
     * @return The latest release version of ChromeDriver
     * @throws IOException            In case of a network error
     * @throws NoSuchElementException When the LATEST_RELEASE file has not been found
     */
    @Nonnull
    private String getLatestReleaseVersion(final List<GoogleCloudStorageEntry> directoryEntries) throws IOException {
        final String latestReleaseFileUrl = directoryEntries
                .stream()
                .filter(entry -> entry.getKey().equals("LATEST_RELEASE"))
                .findAny()
                .map(GoogleCloudStorageEntry::getUrl)
                .orElseThrow(() -> new NoSuchElementException("Unable to determine latest release version: No LATEST_RELEASE file found in directory"));

        return httpClient.execute(new HttpGet(latestReleaseFileUrl),
                httpResponse -> EntityUtils.toString(httpResponse.getEntity()).trim());
    }

}
