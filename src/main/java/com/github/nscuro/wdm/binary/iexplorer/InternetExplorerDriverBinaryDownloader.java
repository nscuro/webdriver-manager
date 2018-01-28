package com.github.nscuro.wdm.binary.iexplorer;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryIsFile;
import static com.github.nscuro.wdm.binary.BinaryExtractor.FileSelectors.entryNameStartsWithIgnoringCase;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_X_ZIP_COMPRESSED;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.lang.String.format;

/**
 * A {@link BinaryDownloader} for Microsoft's Internet Explorer Driver.
 */
public class InternetExplorerDriverBinaryDownloader implements BinaryDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternetExplorerDriverBinaryDownloader.class);

    private static final String BASE_URL = "https://selenium-release.storage.googleapis.com/";

    private static final String BINARY_NAME = "IEDriverServer";

    private final HttpClient httpClient;

    public InternetExplorerDriverBinaryDownloader(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsBrowser(final Browser browser) {
        return Browser.INTERNET_EXPLORER == browser;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        requireWindowsOs(os);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.INTERNET_EXPLORER, version, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("InternetExplorer Driver v{} was already downloaded", version);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading InternetExplorer Driver v{}", version);
        }

        final InternetExplorerRelease matchingRelease = getAvailableReleases()
                .stream()
                .filter(release -> release.getVersion().equals(version))
                .filter(release -> release.getArchitecture() == architecture)
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        format("No binary available for version %s and architecture %s", version, architecture)));

        return BinaryExtractor
                .fromArchiveFile(downloadArchivedRelease(matchingRelease))
                .unZip(destinationFilePath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException {
        requireWindowsOs(os);

        final InternetExplorerRelease latestRelease = getAvailableReleases()
                .stream()
                .filter(release -> release.getArchitecture() == architecture)
                .max(Comparator.comparing(InternetExplorerRelease::getVersion))
                .orElseThrow(() -> new NoSuchElementException(
                        format("Unable to determine latest release for architecture %s", architecture)));

        final String version = latestRelease.getVersion();
        LOGGER.debug("Latest InternetExplorer Driver version is {}", version);

        final Path destinationFilePath = FileUtils.buildBinaryDestinationPath(Browser.INTERNET_EXPLORER, version, os, architecture, destinationDirPath);
        if (destinationFilePath.toFile().exists()) {
            LOGGER.debug("InternetExplorer Driver v{} was already downloaded", version);

            return destinationFilePath.toFile();
        } else {
            LOGGER.debug("Downloading InternetExplorer Driver v{}", version);
        }

        return BinaryExtractor
                .fromArchiveFile(downloadArchivedRelease(latestRelease))
                .unZip(destinationFilePath, entryIsFile().and(entryNameStartsWithIgnoringCase(BINARY_NAME)));
    }

    @Nonnull
    private List<InternetExplorerRelease> getAvailableReleases() throws IOException {
        return httpClient.execute(new HttpGet(BASE_URL), httpResponse -> {
            final List<InternetExplorerRelease> availableReleases = new ArrayList<>();

            try (final InputStream inputStream = httpResponse.getEntity().getContent()) {
                final Document document = Jsoup
                        .parse(inputStream, StandardCharsets.UTF_8.name(), BASE_URL, Parser.xmlParser());

                document
                        .select("Contents > Key")
                        .stream()
                        .filter(element -> element.text().contains(BINARY_NAME))
                        .map(versionLink -> {
                            final String version = versionLink.text().split("/")[0];

                            final Architecture architecture;
                            if (versionLink.text().contains("x64")) {
                                architecture = Architecture.X64;
                            } else if (versionLink.text().toLowerCase().contains("win32")) {
                                architecture = Architecture.X86;
                            } else {
                                LOGGER.warn("Unable to detect architecture from \"{}\"", versionLink.text());
                                return null;
                            }

                            return new InternetExplorerRelease(version, architecture, BASE_URL + versionLink.text());
                        })
                        .filter(Objects::nonNull)
                        .forEach(availableReleases::add);
            }

            return availableReleases;
        });
    }

    @Nonnull
    private File downloadArchivedRelease(final InternetExplorerRelease release) throws IOException {
        final HttpGet request = new HttpGet(release.getDownloadUrl());
        request.setHeader(HttpHeaders.ACCEPT, format("%s,%s", APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED));

        final Path targetFilePath = Files.createTempFile(format("%s_%s-%s", BINARY_NAME, release.getVersion(), release.getArchitecture()), null);
        LOGGER.debug("Downloading archived release to {}", targetFilePath);

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);
            verifyContentTypeIsAnyOf(httpResponse, APPLICATION_ZIP);

            try (final FileOutputStream fileOutputStream = new FileOutputStream(targetFilePath.toFile())) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Body of response to download request is empty"))
                        .writeTo(fileOutputStream);
            }

            return targetFilePath.toFile();
        });
    }

    private void requireWindowsOs(final Os os) {
        if (os != Os.WINDOWS) {
            throw new IllegalArgumentException("Internet Explorer Driver is only supported on Windows");
        }
    }

}
