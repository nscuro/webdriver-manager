package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Set;

import static java.lang.String.format;

final class BinaryManagerImpl implements BinaryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryManagerImpl.class);

    private static final Path BINARY_DESTINATION_DIR_PATH = Paths.get(System.getProperty("user.home"), ".webdriver-manager");

    private final Set<BinaryDownloader> binaryDownloaders;

    BinaryManagerImpl(final Set<BinaryDownloader> binaryDownloaders) {
        this.binaryDownloaders = binaryDownloaders;
    }

    @Nonnull
    @Override
    public File getBinary(final Browser browser, final String version, final Os os, final Architecture architecture) throws IOException {
        final BinaryDownloader binaryDownloader = findBinaryDownloaderForBrowser(browser);

        FileUtils.ensureExistenceOfDir(BINARY_DESTINATION_DIR_PATH);

        final File binaryFile;
        if (version.toLowerCase().equals("latest")) {
            binaryFile = binaryDownloader.downloadLatest(os, architecture, BINARY_DESTINATION_DIR_PATH);
        } else {
            binaryFile = binaryDownloader.download(version, os, architecture, BINARY_DESTINATION_DIR_PATH);
        }

        return binaryFile;
    }

    @Override
    public void registerBinary(final File binaryFile, final Browser browser) {
        if (!binaryFile.exists()) {
            throw new IllegalArgumentException(format("Cannot register binary for %s: %s does not exist", browser, binaryFile));
        } else if (binaryFile.isDirectory()) {
            throw new IllegalArgumentException(format("Cannot register binary for %s: %s is a directory", browser, binaryFile));
        } else {
            LOGGER.info("Registering {} as binary for {}", binaryFile, browser);

            System.setProperty(browser.getBinarySystemProperty(), binaryFile.getAbsolutePath());
        }
    }

    @Nonnull
    private BinaryDownloader findBinaryDownloaderForBrowser(final Browser browser) {
        return binaryDownloaders.stream()
                .filter(downloader -> downloader.supportsBrowser(browser))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

}
