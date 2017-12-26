package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;

final class BinaryManagerImpl implements BinaryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryManagerImpl.class);

    private static final Path BINARY_DESTINATION_DIR_PATH = Paths.get(System.getProperty("user.home"), ".webdriver-manager");

    private final Set<BinaryDownloader> binaryDownloaders;

    BinaryManagerImpl(final Set<BinaryDownloader> binaryDownloaders) {
        this.binaryDownloaders = binaryDownloaders;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File getBinary(final Browser browser, final String version, final Os os, final Architecture architecture) throws IOException {
        final BinaryDownloader binaryDownloader = findBinaryDownloaderForBrowser(browser);

        FileUtils.ensureExistenceOfDir(BINARY_DESTINATION_DIR_PATH);

        final File binaryFile;
        if (version.toLowerCase().equals("latest")) {
            binaryFile = binaryDownloader.downloadLatest(os, architecture, BINARY_DESTINATION_DIR_PATH);
        } else {
            binaryFile = binaryDownloader.download(version, os, architecture, BINARY_DESTINATION_DIR_PATH);
        }

        FileUtils.makeFileExecutable(binaryFile);

        return binaryFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBinary(final File binaryFile, final Browser browser) {
        if (!binaryFile.exists()) {
            throw new IllegalArgumentException(format("Cannot register binary for %s: %s does not exist", browser, binaryFile));
        } else if (binaryFile.isDirectory()) {
            throw new IllegalArgumentException(format("Cannot register binary for %s: %s is a directory", browser, binaryFile));
        } else {
            final String binarySystemProperty = browser.getBinarySystemProperty()
                    .orElseThrow(() -> new UnsupportedOperationException(
                            format("Browser %s does not define a binary system property", browser)));

            System.setProperty(binarySystemProperty, binaryFile.getAbsolutePath());

            LOGGER.info("{} was registered as binary for {}", binaryFile, browser);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUp() {
        Optional.ofNullable(BINARY_DESTINATION_DIR_PATH.toFile().listFiles(BinaryManagerImpl::isWebDriverBinary))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(file -> {
                    if (file.delete()) {
                        // File was deleted, we don't need its reference anymore
                        LOGGER.debug("{} deleted", file);
                        return null;
                    } else {
                        // Deletion failed, keep reference to provide meaningful warning
                        return file;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(file -> LOGGER.warn("{} was not deleted", file));
    }

    @Nonnull
    private BinaryDownloader findBinaryDownloaderForBrowser(final Browser browser) {
        return binaryDownloaders.stream()
                .filter(downloader -> downloader.supportsBrowser(browser))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        format("No binary downloader for browser \"%s\" available", browser)));
    }

    private static boolean isWebDriverBinary(final File file) {
        return file.isFile() && file.getName().startsWith("driver_");
    }

}
