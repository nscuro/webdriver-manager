package com.github.nscuro.wdm.binary.edge;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryProvider;
import com.github.nscuro.wdm.binary.util.MimeType;
import com.github.nscuro.wdm.binary.util.VersionComparator;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link BinaryProvider} for Microsoft's WebDriver implementation.
 *
 * @see <a href="https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/">Microsoft WebDriver homepage</a>
 * @since 0.2.0
 */
public class MicrosoftWebDriverBinaryProvider implements BinaryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftWebDriverBinaryProvider.class);

    private static final Pattern VERSION_PATTERN = Pattern.compile("Version: ([0-9|.]+) \\|.*");

    private final HttpClient httpClient;

    private final String binaryDownloadPageUrl;

    public MicrosoftWebDriverBinaryProvider(final HttpClient httpClient) {
        this(requireNonNull(httpClient, "no HttpClient provided"),
                "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/");
    }

    MicrosoftWebDriverBinaryProvider(final HttpClient httpClient, final String binaryDownloadPageUrl) {
        this.httpClient = httpClient;
        this.binaryDownloadPageUrl = binaryDownloadPageUrl;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false} for every {@link Browser} except {@link Browser#EDGE},
     *         in which case {@code true} is returned
     */
    @Override
    public boolean providesBinaryForBrowser(final Browser browser) {
        return Browser.EDGE == browser;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Optional#empty()} for every {@link Os} except {@link Os#WINDOWS},
     *         in which case the latest binary version is returned
     */
    @Nonnull
    @Override
    public Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException {
        if (os != Os.WINDOWS) {
            LOGGER.warn("Microsoft WebDriver is only supported on Windows systems");
            return Optional.empty();
        }

        return getAvailableReleases()
                .stream()
                .map(MicrosoftWebDriverRelease::getVersion)
                .max(new VersionComparator());
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException When requesting an {@link Os} other than {@link Os#WINDOWS}
     * @throws NoSuchElementException        When no binary is available for the requested version
     */
    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException {
        if (os != Os.WINDOWS) {
            throw new UnsupportedOperationException("Microsoft WebDriver is only supported on Windows systems");
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
                Optional
                        .ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IOException(format("Response body is empty. Response was:\n%s", httpResponse)))
                        .writeTo(fileOutputStream);
            }

            return binaryDestinationFile;
        });
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Browser.EDGE);
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

        return ((BinaryProvider) otherObject).providesBinaryForBrowser(Browser.EDGE);
    }

    @Nonnull
    private List<MicrosoftWebDriverRelease> getAvailableReleases() throws IOException {
        final List<MicrosoftWebDriverRelease> availableReleases = new ArrayList<>();

        return httpClient.execute(new HttpGet(binaryDownloadPageUrl), httpResponse -> {
            try (final InputStream inputStream = httpResponse.getEntity().getContent()) {
                final Document document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), binaryDownloadPageUrl);

                document.select("div.module:nth-of-type(2) > ul.driver-downloads > li.driver-download").forEach(releaseElement -> {
                    final Optional<String> downloadUrl = Optional
                            .ofNullable(releaseElement.selectFirst("a"))
                            .map(linkElement -> linkElement.attr("href"));

                    final Optional<String> downloadMeta = Optional
                            .ofNullable(releaseElement.selectFirst("p.driver-download__meta"))
                            .map(Element::text);

                    if (downloadUrl.isPresent() && downloadMeta.isPresent()) {
                        final Matcher versionMatcher = VERSION_PATTERN.matcher(downloadMeta.get());

                        if (!versionMatcher.matches()) {
                            LOGGER.warn("Unable to parse version from \"{}\"", downloadMeta.get());
                        } else {
                            availableReleases.add(new MicrosoftWebDriverRelease(versionMatcher.group(1), downloadUrl.get()));
                        }
                    }
                });
            }

            return availableReleases;
        });
    }

}
