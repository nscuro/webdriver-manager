package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import com.github.nscuro.wdm.binary.BinaryDownloader;
import com.github.nscuro.wdm.binary.BinaryExtractor;
import com.github.nscuro.wdm.binary.util.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_X_ZIP_COMPRESSED;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link BinaryDownloader} for Google's <a href="https://sites.google.com/a/chromium.org/chromedriver/">ChromeDriver</a>.
 */
@Deprecated
public final class ChromeDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeDriverBinaryDownloader.class);

    private static final String BASE_URL = "https://chromedriver.storage.googleapis.com";

    private static final String BINARY_NAME = "chromedriver";

    private final HttpClient httpClient;

    public ChromeDriverBinaryDownloader(final HttpClient httpClient) {
        this.httpClient = requireNonNull(httpClient, "No HttpClient provided");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.CHROME == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final Platform driverPlatform = ChromeDriverPlatform.valueOf(os, architecture);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.CHROME, version, os, architecture, destinationDirPath);

        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("ChromeDriver v{} for {} was already downloaded", version, driverPlatform.getName());

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading ChromeDriver v{} for {}", version, driverPlatform.getName());
        }

        return BinaryExtractor
                .fromArchiveFile(downloadArchivedBinary(version, driverPlatform))
                .unZip(destinationFilePath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        LOGGER.debug("Downloading latest ChromeDriver");

        return download(getLatestVersion(), os, architecture, destinationDirPath);
    }

    @Nonnull
    String getLatestVersion() throws IOException {
        return httpClient.execute(new HttpGet(BASE_URL + "/LATEST_RELEASE"), httpResponse -> {
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Unexpected response code");
            } else if (httpResponse.getEntity() == null) {
                throw new RuntimeException("Response body is empty");
            } else {
                final String latestReleaseVersion = EntityUtils.toString(httpResponse.getEntity()).trim();
                LOGGER.debug("Latest ChromeDriver version is {}", latestReleaseVersion);
                return latestReleaseVersion;
            }
        });
    }

    @Nonnull
    private File downloadArchivedBinary(final String version, final Platform platform) throws IOException {
        final HttpGet request = new HttpGet(format("%s/%s/chromedriver_%s.zip", BASE_URL, version, platform.getName()));
        request.setHeader(HttpHeaders.ACCEPT, format("%s,%s", APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED));

        final Path targetFilePath = Files.createTempFile(format("chromedriver_%s-%s_", version, platform.getName()), null);
        LOGGER.debug("Downloading archived binary to {}", targetFilePath);

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new NoSuchElementException(
                        format("No ChromeDriver binary available for version %s and platform %s", version, platform));
            }

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
