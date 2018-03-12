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
import java.util.NoSuchElementException;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @since 0.1.5
 */
public class BinaryManagerV2Impl implements BinaryManagerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryManagerV2Impl.class);

    private Path binaryDestinationDirPath;

    private final Set<BinaryProvider> binaryProviders;

    BinaryManagerV2Impl(final Path binaryDestinationDirPath,
                        final Set<BinaryProvider> binaryProviders) {
        this.binaryDestinationDirPath = validateBinaryDestinationDirPath(binaryDestinationDirPath);
        this.binaryProviders = binaryProviders;
    }

    @Nonnull
    @Override
    public File getWebDriverBinary(final Browser browser,
                                   @Nullable final String version,
                                   final Os os,
                                   final Architecture architecture) {
        final BinaryProvider binaryProvider = getBinaryProviderForBrowser(browser);

        final String versionToDownload;

        if (version == null) {
            try {
                versionToDownload = binaryProvider
                        .getLatestBinaryVersion(os, architecture)
                        .orElseThrow(NoSuchElementException::new);

                LOGGER.info("Latest version of {}'s WebDriver binary is {}", browser, versionToDownload);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            versionToDownload = version;
        }

        final Path binaryDestinationFilePath = buildBinaryDestinationFilePath(browser, versionToDownload, os, architecture);

        final File webDriverBinaryFile;

        if (!binaryDestinationFilePath.toFile().exists()) {
            try {
                webDriverBinaryFile = binaryProvider.download(versionToDownload, os, architecture, binaryDestinationFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.info("{} already exists - Nothing to download", binaryDestinationFilePath);
            webDriverBinaryFile = binaryDestinationFilePath.toFile();
        }

        if (!webDriverBinaryFile.setExecutable(true)) {
            LOGGER.warn("{} was not made executable. "
                            + "You probably don't have sufficient permissions in your chosen binary destination directory",
                    webDriverBinaryFile);
        }

        return webDriverBinaryFile;
    }

    @Override
    public void registerWebDriverBinary(final Browser browser, final File webDriverBinaryFile) {

    }

    @Nonnull
    private Path validateBinaryDestinationDirPath(final Path binaryDestinationDirPath) {
        final File fileHandle = requireNonNull(binaryDestinationDirPath).toFile();

        if (!fileHandle.exists()) {
            throw new IllegalArgumentException(format("\"%s\" does not exist", fileHandle));
        } else if (!fileHandle.isDirectory()) {
            throw new IllegalArgumentException(format("\"%s\" is not a directory", fileHandle));
        } else if (!fileHandle.canWrite()) {
            throw new IllegalArgumentException(format("no write permissions for \"%s\"", fileHandle));
        } else {
            return binaryDestinationDirPath;
        }
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
                .resolve(format("wdm-webdriver_%s_%s-%s_%s", browser.name(),
                        os.name(), architecture.name(), version).toLowerCase());
    }

}
