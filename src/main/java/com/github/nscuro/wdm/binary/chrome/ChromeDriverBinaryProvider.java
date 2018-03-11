package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
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

/**
 * @since 0.1.5
 */
public class ChromeDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeDriverBinaryProvider.class);

    private static final String DEFAULT_GCS_DIRECTORY_URL = "https://chromedriver.storage.googleapis.com/";

    private static final String BINARY_NAME = "chromedriver";

    private final GoogleCloudStorageDirectory cloudStorageDirectory;

    private final BinaryExtractorFactory binaryExtractorFactory;

    public ChromeDriverBinaryProvider(final HttpClient httpClient) {
        this(new GoogleCloudStorageDirectory(httpClient, DEFAULT_GCS_DIRECTORY_URL), new BinaryExtractorFactory());
    }

    ChromeDriverBinaryProvider(final GoogleCloudStorageDirectory cloudStorageDirectory,
                               final BinaryExtractorFactory binaryExtractorFactory) {
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
        final Platform platform = ChromeDriverPlatform.valueOf(os, architecture);

        return cloudStorageDirectory
                .getEntries()
                .stream()
                .map(GoogleCloudStorageEntry::getKey)
                .map(String::toLowerCase)
                .filter(key -> key.contains(platform.getName()))
                .map(key -> key.split("/")[0])
                .max(Comparator.naturalOrder());
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        final Platform platform = ChromeDriverPlatform.valueOf(os, architecture);

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

}
