package com.github.nscuro.wdm.binary.util.compression;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * @since 0.1.5
 */
final class ZipBinaryExtractor implements BinaryExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipBinaryExtractor.class);

    private final File archiveFile;

    ZipBinaryExtractor(final File archiveFile) {
        this.archiveFile = requireNonNull(archiveFile);
    }

    @Nonnull
    @Override
    public File extractBinary(final Path binaryDestinationPath, final Predicate<ArchiveEntry> binaryEntrySelector) throws IOException {
        if (!archiveFile.exists() || !archiveFile.canRead()) {
            throw new IllegalStateException(format("\"%s\" does not exist or is not readable", archiveFile));
        }

        try (final InputStream fileInputStream = Files.newInputStream(archiveFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
             final InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             final ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(bufferedInputStream)) {

            for (ZipArchiveEntry zipEntry = zipInputStream.getNextZipEntry();
                 !isNull(zipEntry);
                 zipEntry = zipInputStream.getNextZipEntry()) {

                if (binaryEntrySelector.test(zipEntry)) {
                    Files.copy(zipInputStream, binaryDestinationPath, StandardCopyOption.REPLACE_EXISTING);

                    LOGGER.debug("extracted to {}", binaryDestinationPath);

                    return binaryDestinationPath.toFile();
                }
            }
        }

        throw new NoSuchElementException("No binary file was extracted");
    }

}
