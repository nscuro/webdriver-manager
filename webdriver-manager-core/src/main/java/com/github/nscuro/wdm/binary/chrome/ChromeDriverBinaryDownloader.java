package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryDownloader;
import com.github.nscuro.wdm.binary.CompressionUtils;
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
import java.nio.file.Path;

import static java.lang.String.format;

public final class ChromeDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeDriverBinaryDownloader.class);

    private static final String BASE_URL = "https://chromedriver.storage.googleapis.com";

    private final HttpClient httpClient;

    public ChromeDriverBinaryDownloader(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.CHROME.equals(browser);
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        final ChromeDriverPlatform chromeDriverPlatform = ChromeDriverPlatform.from(os, architecture);

        final String binaryName = buildChromeDriverBinaryName(version, chromeDriverPlatform);

        if (destinationDirPath.resolve(binaryName).toFile().exists()) {
            LOGGER.info("ChromeDriver v{} for {} was already downloaded", version, chromeDriverPlatform);
            return destinationDirPath.resolve(binaryName).toFile();
        } else {
            LOGGER.info("Downloading ChromeDriver v{} for {}", version, chromeDriverPlatform);
        }

        final HttpGet request = new HttpGet(BASE_URL + format("/%s/chromedriver_%s.zip", version, chromeDriverPlatform.name().toLowerCase()));
        request.setHeader(HttpHeaders.ACCEPT, "application/zip,application/x-zip-compressed");

        final byte[] zippedBinaryContent = httpClient.execute(request, httpResponse -> {
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException();
            } else if (httpResponse.getEntity() == null) {
                throw new RuntimeException();
            } else {
                return EntityUtils.toByteArray(httpResponse.getEntity());
            }
        });

        final File binaryFile = CompressionUtils.unzipFile(zippedBinaryContent, destinationDirPath.resolve(binaryName),
                zipEntry -> !zipEntry.isDirectory() && zipEntry.getName().toLowerCase().contains("chromedriver"))
                .orElseThrow(RuntimeException::new);

        if (!binaryFile.setExecutable(true)) {
            LOGGER.warn("Failed to make {} executable", binaryFile);
        }

        return binaryFile;
    }

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
    private String buildChromeDriverBinaryName(final String version, final ChromeDriverPlatform chromeDriverPlatform) {
        return format("chromedriver-%s_%s", version, chromeDriverPlatform.name().toLowerCase());
    }

}
