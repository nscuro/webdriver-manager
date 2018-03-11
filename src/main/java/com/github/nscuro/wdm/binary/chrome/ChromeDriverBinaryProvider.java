package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.compression.BinaryExtractorFactory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageDirectory;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.util.compression.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static java.lang.String.format;

/**
 * @since 0.1.5
 */
public class ChromeDriverBinaryProvider implements BinaryProvider {

    private static final String DEFAULT_GCS_DIRECTORY_URL = "https://chromedriver.storage.googleapis.com/";

    private static final String BINARY_NAME = "chromedriver";

    private final HttpClient httpClient;

    private final GoogleCloudStorageDirectory cloudStorageDirectory;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public ChromeDriverBinaryProvider(final HttpClient httpClient) {
        this(httpClient,
                new GoogleCloudStorageDirectory(httpClient, DEFAULT_GCS_DIRECTORY_URL),
                new BinaryExtractorFactory());
    }

    ChromeDriverBinaryProvider(final HttpClient httpClient,
                               final GoogleCloudStorageDirectory cloudStorageDirectory,
                               final BinaryExtractorFactory binaryExtractorFactory) {
        this.httpClient = httpClient;
        this.cloudStorageDirectory = cloudStorageDirectory;
        this.binaryExtractorFactory = binaryExtractorFactory;
    }

    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.CHROME == browser;
    }

    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final Optional<ChromeDriverPlatform> platform = ChromeDriverPlatform.valueOf2(os, architecture);

        if (!platform.isPresent()) {
            return Optional.empty();
        }

        final List<GoogleCloudStorageEntry> cloudStorageEntries = cloudStorageDirectory.getEntries();

        final String latestReleaseVersion = getLatestReleaseVersion(cloudStorageEntries);

        return cloudStorageDirectory
                .getEntries()
                .stream()
                .map(GoogleCloudStorageEntry::getKey)
                .map(String::toLowerCase)
                .filter(key -> key.contains(platform.get().getName()))
                .map(key -> key.split("/")[0])
                // For whatever reason there are versions higher than LATEST_RELEASE in the directory
                // that are older than any of those equal to or lower than LATEST_RELEASE...
                .filter(version -> Comparator.comparing(String::trim).compare(version, latestReleaseVersion) <= 0)
                .max(Comparator.naturalOrder());
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        final ChromeDriverPlatform platform = ChromeDriverPlatform.valueOf2(os, architecture)
                .orElseThrow(() -> new UnsupportedOperationException(format("%s %s is not supported by ChromeDriver", os, architecture)));

        final String downloadUrl = cloudStorageDirectory
                .getEntries()
                .stream()
                .filter(entry -> entry.getKey().contains(version))
                .filter(entry -> entry.getKey().contains(platform.getName()))
                .findAny()
                .map(GoogleCloudStorageEntry::getUrl)
                .orElseThrow(NoSuchElementException::new);

        return binaryExtractorFactory
                .getBinaryExtractorForArchiveFile(cloudStorageDirectory.downloadFile(downloadUrl))
                .extractBinary(binaryDestinationPath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    /**
     * Determine the latest available release version.
     * <p>
     * It is possible that this version is not the "latest" version for all platform,
     * e.g. {@link ChromeDriverPlatform#LINUX32}'s latest version is {@code 2.33}, even though
     * the latest version for all other platforms is {@code 2.36}.
     *
     * @param directoryEntries Entries of the {@link GoogleCloudStorageDirectory}
     * @return The latest release version of ChromeDriver
     * @throws IOException
     * @throws IllegalStateException When no {@code LATEST_RELEASE} file was found in the provided directory entries
     */
    @Nonnull
    private String getLatestReleaseVersion(final List<GoogleCloudStorageEntry> directoryEntries) throws IOException {
        final String latestReleaseFileUrl = directoryEntries
                .stream()
                .filter(entry -> entry.getKey().equals("LATEST_RELEASE"))
                .findAny()
                .map(GoogleCloudStorageEntry::getUrl)
                .orElseThrow(() -> new IllegalStateException("Unable to determine latest release version"));

        return httpClient.execute(new HttpGet(latestReleaseFileUrl),
                httpResponse -> EntityUtils.toString(httpResponse.getEntity()));
    }

}
