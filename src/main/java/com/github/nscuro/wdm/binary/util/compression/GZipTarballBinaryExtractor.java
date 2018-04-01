package com.github.nscuro.wdm.binary.util.compression;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
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

/**
 * A {@link BinaryExtractor} for gzipped tarball archives.
 *
 * @since 0.2.0
 */
final class GZipTarballBinaryExtractor implements BinaryExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GZipTarballBinaryExtractor.class);

    private final File archiveFile;

    GZipTarballBinaryExtractor(final File archiveFile) {
        this.archiveFile = archiveFile;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public File extractBinary(final Path binaryDestinationPath, final Predicate<ArchiveEntry> binaryEntrySelector) throws IOException {
        if (!archiveFile.exists() || !archiveFile.canRead()) {
            throw new IllegalStateException(format("\"%s\" does not exist or is not readable", archiveFile));
        }

        try (final InputStream fileInputStream = Files.newInputStream(archiveFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
             final InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             final GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(bufferedInputStream);
             final TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            for (TarArchiveEntry tarEntry = tarInputStream.getNextTarEntry();
                 !isNull(tarEntry);
                 tarEntry = tarInputStream.getNextTarEntry()) {

                if (binaryEntrySelector.test(tarEntry)) {
                    Files.copy(tarInputStream, binaryDestinationPath, StandardCopyOption.REPLACE_EXISTING);

                    LOGGER.debug("extracted to {}", binaryDestinationPath);

                    return binaryDestinationPath.toFile();
                }
            }
        }

        throw new NoSuchElementException("Nothing was extracted");
    }

}
