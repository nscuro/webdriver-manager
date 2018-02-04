package com.github.nscuro.wdm.binary.edge;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryDownloader;
import com.github.nscuro.wdm.binary.util.FileUtils;
import com.github.nscuro.wdm.binary.util.MimeType;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static java.lang.String.format;

/**
 * A {@link BinaryDownloader} for Microsoft's <a href="https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/">Microsoft WebDriver</a>.
 *
 * @since 0.1.2
 */
public final class MicrosoftWebDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWebDriverBinaryDownloader.class);

    private static final String BASE_URL = "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";

    private static final Pattern VERSION_PATTERN = Pattern.compile("Version: ([0-9|.]+) \\|.*");

    private final HttpClient httpClient;

    public MicrosoftWebDriverBinaryDownloader(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.EDGE == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        requireWindowsOs(os);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.EDGE, version, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("Microsoft WebDriver v{} was already downloaded", version);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading Microsoft WebDriver v{}", version);
        }

        final MicrosoftWebDriverRelease matchingRelease = getAvailableReleases().stream()
                .filter(release -> release.getVersion().equals(version))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(format("Version \"%s\" of Microsoft WebDriver is not available", version)));

        return downloadRelease(matchingRelease, destinationFilePath.toFile());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        requireWindowsOs(os);

        final MicrosoftWebDriverRelease latestRelease = getAvailableReleases()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot determine latest Microsoft WebDriver release"));
        LOGGER.debug("Latest Microsoft WebDriver version is {}", latestRelease.getVersion());

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.EDGE, latestRelease.getVersion(), os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("Microsoft WebDriver v{} was already downloaded", latestRelease.getVersion());

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading Microsoft WebDriver v{}", latestRelease.getVersion());
        }

        return downloadRelease(latestRelease, destinationFilePath.toFile());
    }

    @Nonnull
    private List<MicrosoftWebDriverRelease> getAvailableReleases() throws IOException {
        final List<MicrosoftWebDriverRelease> availableReleases = new ArrayList<>();

        return httpClient.execute(new HttpGet(BASE_URL), httpResponse -> {
            try (final InputStream inputStream = httpResponse.getEntity().getContent()) {
                final Document document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), BASE_URL);

                document.select("li.driver-download").forEach(releaseElement -> {
                    final String downloadUrl = releaseElement.selectFirst("a").attr("href");
                    final String downloadMeta = releaseElement.selectFirst("p.driver-download__meta").text();

                    final Matcher versionMatcher = VERSION_PATTERN.matcher(downloadMeta);
                    if (!versionMatcher.matches()) {
                        LOGGER.trace("Unable to parse version from \"{}\"", downloadMeta);
                    } else {
                        availableReleases.add(new MicrosoftWebDriverRelease(versionMatcher.group(1), downloadUrl));
                    }
                });
            }

            return availableReleases;
        });
    }

    @Nonnull
    private File downloadRelease(final MicrosoftWebDriverRelease release, final File destinationFile) throws IOException {
        return httpClient.execute(new HttpGet(release.getDownloadUrl()), httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);
            verifyContentTypeIsAnyOf(httpResponse, MimeType.APPLICATION_OCTET_STREAM);

            try (final FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Body of response to download request is empty"))
                        .writeTo(fileOutputStream);
            }

            return destinationFile;
        });
    }

    void requireWindowsOs(final Os os) {
        if (os != Os.WINDOWS) {
            throw new IllegalArgumentException("Microsoft WebDriver is only supported on Windows");
        }
    }

}
