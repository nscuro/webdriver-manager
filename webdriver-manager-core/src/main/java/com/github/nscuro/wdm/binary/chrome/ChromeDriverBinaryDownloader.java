package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_X_ZIP_COMPRESSED;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.lang.String.format;

public final class ChromeDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeDriverBinaryDownloader.class);

    private static final String BASE_URL = "https://chromedriver.storage.googleapis.com";

    private final HttpClient httpClient;

    public ChromeDriverBinaryDownloader(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.CHROME.equals(browser);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final ChromeDriverPlatform driverPlatform = ChromeDriverPlatform.valueOf(os, architecture);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.CHROME, version, os, architecture, destinationDirPath);

        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("ChromeDriver v{} for {} was already downloaded", version, driverPlatform);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading ChromeDriver v{} for {}", version, driverPlatform);
        }

        try (final BinaryExtractor binaryExtractor = BinaryExtractor.fromArchiveFile(downloadArchivedBinary(version, driverPlatform))) {
            return binaryExtractor.unZip(destinationFilePath,
                    entryIsFile().and(entryNameStartsWithIgnoringCase("chromedriver")));
        }
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
    File downloadArchivedBinary(final String version, final ChromeDriverPlatform platform) throws IOException {
        final HttpGet request = new HttpGet(format("%s/%s/chromedriver_%s.zip", BASE_URL, version, platform.name().toLowerCase()));
        request.setHeader(HttpHeaders.ACCEPT, format("%s,%s", APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED));

        final File targetFile = FileUtils.getTempDirPath()
                .resolve(format("chromedriver-%s_%s.zip", version, platform.name().toLowerCase())).toFile();
        LOGGER.debug("Downloading archived binary to {}", targetFile);

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);
            verifyContentTypeIsAnyOf(httpResponse, APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED);

            try (final FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Response body was empty"))
                        .writeTo(fileOutputStream);
            }

            return targetFile;
        });
    }

}
