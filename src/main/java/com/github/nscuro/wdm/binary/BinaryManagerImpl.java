package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @since 0.2.0
 */
final class BinaryManagerImpl implements BinaryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryManagerImpl.class);

    static final String WEB_DRIVER_BINARY_PREFIX = "wdm-webdriver";

    private Path binaryDestinationDirPath;

    private final Set<BinaryProvider> binaryProviders;

    BinaryManagerImpl(final Path binaryDestinationDirPath,
                      final Set<BinaryProvider> binaryProviders) {
        this.binaryDestinationDirPath = validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPath);
        this.binaryProviders = binaryProviders;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public synchronized File getWebDriverBinary(final Browser browser,
                                   @Nullable final String version,
                                   final Os os,
                                   final Architecture architecture) throws IOException {
        final BinaryProvider binaryProvider = getBinaryProviderForBrowser(browser);

        final String versionToDownload;

        if (version == null) {
            versionToDownload = binaryProvider
                    .getLatestBinaryVersion(os, architecture)
                    .orElseThrow(NoSuchElementException::new);

            LOGGER.info("Latest version of {}'s WebDriver binary for {} {} is {}",
                    browser, os, architecture, versionToDownload);
        } else {
            versionToDownload = version;
        }

        final Path binaryDestinationFilePath = buildBinaryDestinationFilePath(browser, versionToDownload, os, architecture);

        final File webDriverBinaryFile;

        if (!binaryDestinationFilePath.toFile().exists()) {
            webDriverBinaryFile = binaryProvider.download(versionToDownload, os, architecture, binaryDestinationFilePath);
        } else {
            LOGGER.info("{} already exists - Nothing to download", binaryDestinationFilePath);
            webDriverBinaryFile = binaryDestinationFilePath.toFile();
        }

        if (!webDriverBinaryFile.setExecutable(true)) {
            LOGGER.warn("{} couldn't be made executable. "
                            + "You probably don't have sufficient permissions in your chosen binary destination directory",
                    webDriverBinaryFile);
        }

        return webDriverBinaryFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void registerWebDriverBinary(final Browser browser, final File webDriverBinaryFile) {
        if (!webDriverBinaryFile.exists()) {
            throw new IllegalArgumentException(format("Cannot register WebDriver binary for %s: %s does not exist",
                    browser, webDriverBinaryFile));
        } else if (webDriverBinaryFile.isDirectory()) {
            throw new IllegalArgumentException(format("Cannot register WebDriver binary for %s: %s is a directory",
                    browser, webDriverBinaryFile));
        }

        final String binarySystemProperty = browser.getBinarySystemProperty()
                .orElseThrow(() -> new UnsupportedOperationException(
                        format("Cannot register WebDriver binary for %s: No binary system property", browser)));

        System.setProperty(binarySystemProperty, webDriverBinaryFile.getAbsolutePath());

        LOGGER.info("Registered \"{}\" as WebDriver binary for {}", webDriverBinaryFile.getAbsolutePath(), browser);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public List<File> getLocalWebDriverBinaries() {
        return Optional
                .ofNullable(binaryDestinationDirPath.toFile().listFiles(this::isWebDriverBinary))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());
    }

    @Nonnull
    Path validateAndPrepareBinaryDestinationDirPath(final Path binaryDestinationDirPath) {
        final File fileHandle = requireNonNull(binaryDestinationDirPath).toFile();

        if (fileHandle.exists()) {
            if (!fileHandle.isDirectory()) {
                throw new IllegalArgumentException(format("\"%s\" is not a directory", fileHandle));
            } else if (!fileHandle.canWrite()) {
                throw new IllegalArgumentException(format("no write permissions for \"%s\"", fileHandle));
            }
        } else {
            if (!fileHandle.getParentFile().canWrite()) {
                throw new IllegalArgumentException(
                        format("cannot create directory at \"%s\": no write permissions in parent directory", fileHandle));
            } else if (!fileHandle.mkdirs()) {
                throw new IllegalStateException(format("Directory at \"%s\" has not been created", fileHandle));
            } else {
                LOGGER.info("Created directory at \"{}\"", fileHandle);
            }
        }

        return binaryDestinationDirPath;
    }

    @Nonnull
    private BinaryProvider getBinaryProviderForBrowser(final Browser browser) {
        return binaryProviders
                .stream()
                .filter(provider -> provider.providesBinaryForBrowser(browser))
                .findFirst()
                .orElseThrow(UnsupportedOperationException::new);
    }

    @Nonnull
    private Path buildBinaryDestinationFilePath(final Browser browser, final String version, final Os os, final Architecture architecture) {
        return binaryDestinationDirPath
                .resolve(format("%s_%s_%s-%s_%s", WEB_DRIVER_BINARY_PREFIX, browser.name(),
                        os.name(), architecture.name(), version).toLowerCase());
    }

    boolean isWebDriverBinary(final File file) {
        return file.isFile() && file.getName().startsWith(WEB_DRIVER_BINARY_PREFIX);
    }

}
