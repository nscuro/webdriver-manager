package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import com.github.nscuro.wdm.binary.BinaryExtractor;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.googlecs.GoogleCloudStorageDirectory;
import com.github.nscuro.wdm.binary.googlecs.GoogleCloudStorageEntry;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_X_ZIP_COMPRESSED;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.lang.String.format;

public class ChromeDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeDriverBinaryProvider.class);

    private static final String BINARY_NAME = "chromedriver";

    private final HttpClient httpClient;

    private final GoogleCloudStorageDirectory cloudStorageDirectory;

    public ChromeDriverBinaryProvider(final HttpClient httpClient) {
        this(httpClient, new GoogleCloudStorageDirectory(httpClient, "https://chromedriver.storage.googleapis.com/"));
    }

    ChromeDriverBinaryProvider(final HttpClient httpClient, final GoogleCloudStorageDirectory cloudStorageDirectory) {
        this.httpClient = httpClient;
        this.cloudStorageDirectory = cloudStorageDirectory;
    }

    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.CHROME == browser;
    }

    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        final ChromeDriverPlatform platform = ChromeDriverPlatform.valueOf(os, architecture);

        return cloudStorageDirectory
                .getEntries()
                .stream()
                .map(GoogleCloudStorageEntry::getKey)
                .map(String::toLowerCase)
                .filter(key -> key.contains(platform.getName()))
                .map(key -> key.split("/")[0])
                .sorted()
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

        return BinaryExtractor
                .fromArchiveFile(downloadArchivedBinaryFile(downloadUrl))
                .unZip(binaryDestinationPath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    @Nonnull
    private File downloadArchivedBinaryFile(final String downloadUrl) throws IOException {
        final HttpGet request = new HttpGet(downloadUrl);
        request.setHeader(HttpHeaders.ACCEPT, format("%s,%s", APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED));

        final Path targetFilePath = Files.createTempFile("chromedriver_", null);
        LOGGER.debug("Downloading archived binary to {}", targetFilePath);

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);

            verifyContentTypeIsAnyOf(httpResponse, APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED);

            try (final OutputStream fileOutputStream = Files.newOutputStream(targetFilePath)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Response body was empty"))
                        .writeTo(fileOutputStream);
            }

            return targetFilePath.toFile();
        });
    }

}
