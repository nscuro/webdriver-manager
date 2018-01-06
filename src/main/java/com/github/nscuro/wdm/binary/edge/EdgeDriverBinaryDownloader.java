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

public final class EdgeDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeDriverBinaryDownloader.class);

    private static final String BASE_URL = "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";

    private static final Pattern VERSION_PATTERN = Pattern.compile("Version: ([0-9|.]+) \\|.*");

    private final HttpClient httpClient;

    public EdgeDriverBinaryDownloader(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.EDGE == browser;
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        if (os != Os.WINDOWS) {
            throw new IllegalArgumentException("Edge is only supported for Windows");
        }

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.EDGE, version, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("EdgeDriver v{} was already downloaded", version);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading EdgeDriver v{}", version);
        }

        final EdgeRelease matchingRelease = getAvailableReleases().stream()
                .filter(release -> release.getVersion().equals(version))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(format("No EdgeDriver binary found for version \"%s\"", version)));

        return downloadRelease(matchingRelease, destinationFilePath.toFile());
    }

    @Nonnull
    @Override
    public File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        return download(getAvailableReleases().get(0).getVersion(), os, architecture, destinationDirPath);
    }

    @Nonnull
    private List<EdgeRelease> getAvailableReleases() throws IOException {
        final List<EdgeRelease> availableReleases = new ArrayList<>();

        return httpClient.execute(new HttpGet(BASE_URL), httpResponse -> {
            try (final InputStream inputStream = httpResponse.getEntity().getContent()) {
                final Document document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), BASE_URL);

                document.select("li.driver-download").forEach(releaseElement -> {
                    final String downloadUrl = releaseElement.selectFirst("a").attr("href");
                    final String downloadMeta = releaseElement.selectFirst("p.driver-download__meta").text();

                    final Matcher versionMatcher = VERSION_PATTERN.matcher(downloadMeta);
                    if (!versionMatcher.matches()) {
                        LOGGER.debug("No version found in \"{}\"", downloadMeta);
                    } else {
                        availableReleases.add(new EdgeRelease(versionMatcher.group(1), downloadUrl));
                    }
                });
            }

            return availableReleases;
        });
    }

    @Nonnull
    private File downloadRelease(final EdgeRelease release, final File destinationFile) throws IOException {
        return httpClient.execute(new HttpGet(release.getDownloadUrl()), httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);
            verifyContentTypeIsAnyOf(httpResponse, MimeType.APPLICATION_OCTET_STREAM);

            try (final FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(IllegalStateException::new)
                        .writeTo(fileOutputStream);
            }

            return destinationFile;
        });
    }

}
