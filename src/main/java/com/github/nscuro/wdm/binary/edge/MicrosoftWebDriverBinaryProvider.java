package com.github.nscuro.wdm.binary.edge;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
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
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static java.util.Objects.requireNonNull;

/**
 * @since 0.1.5
 */
public class MicrosoftWebDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWebDriverBinaryProvider.class);

    private static final String DEFAULT_BASE_URL = "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";

    private static final Pattern VERSION_PATTERN = Pattern.compile("Version: ([0-9|.]+) \\|.*");

    private final HttpClient httpClient;

    private final String binaryDownloadPageUrl;

    public MicrosoftWebDriverBinaryProvider(final HttpClient httpClient) {
        this(httpClient, DEFAULT_BASE_URL);
    }

    MicrosoftWebDriverBinaryProvider(final HttpClient httpClient, final String binaryDownloadPageUrl) {
        this.httpClient = requireNonNull(httpClient, "no HTTP client provided");
        this.binaryDownloadPageUrl = requireNonNull(binaryDownloadPageUrl, "no binary download page URL provided");
    }

    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.EDGE == browser;
    }

    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        if (os != Os.WINDOWS) {
            return Optional.empty();
        }

        return getAvailableReleases()
                .stream()
                .map(MicrosoftWebDriverRelease::getVersion)
                .max(Comparator.naturalOrder());
    }

    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        if (os != Os.WINDOWS) {
            throw new UnsupportedOperationException();
        }

        final MicrosoftWebDriverRelease matchingRelease = getAvailableReleases()
                .stream()
                .filter(release -> release.getVersion().equals(version))
                .findAny()
                .orElseThrow(NoSuchElementException::new);

        return httpClient.execute(new HttpGet(matchingRelease.getDownloadUrl()), httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);
            verifyContentTypeIsAnyOf(httpResponse, MimeType.APPLICATION_OCTET_STREAM);

            final File binaryDestinationFile = binaryDestinationPath.toFile();

            try (final FileOutputStream fileOutputStream = new FileOutputStream(binaryDestinationFile)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Body of response to download request is empty"))
                        .writeTo(fileOutputStream);
            }

            return binaryDestinationFile;
        });
    }

    @Nonnull
    private List<MicrosoftWebDriverRelease> getAvailableReleases() throws IOException {
        final List<MicrosoftWebDriverRelease> availableReleases = new ArrayList<>();

        return httpClient.execute(new HttpGet(binaryDownloadPageUrl), httpResponse -> {
            try (final InputStream inputStream = httpResponse.getEntity().getContent()) {
                final Document document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), binaryDownloadPageUrl);

                document.select("li.driver-download").forEach(releaseElement -> {
                    final String downloadUrl = releaseElement.selectFirst("a").attr("href");
                    final String downloadMeta = releaseElement.selectFirst("p.driver-download__meta").text();

                    final Matcher versionMatcher = VERSION_PATTERN.matcher(downloadMeta);
                    if (!versionMatcher.matches()) {
                        LOGGER.warn("Unable to parse version from \"{}\"", downloadMeta);
                    } else {
                        availableReleases.add(new MicrosoftWebDriverRelease(versionMatcher.group(1), downloadUrl));
                    }
                });
            }

            return availableReleases;
        });
    }

}
