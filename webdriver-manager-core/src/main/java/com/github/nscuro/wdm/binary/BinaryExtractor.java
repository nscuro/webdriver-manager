package com.github.nscuro.wdm.binary;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public final class BinaryExtractor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryExtractor.class);

    private final File archiveFile;

    private BinaryExtractor(final File archiveFile) {
        this.archiveFile = archiveFile;
    }

    @Nonnull
    public static BinaryExtractor fromArchiveFile(final File archiveFile) {
        if (!archiveFile.exists()) {
            throw new IllegalArgumentException(format("Archive file %s does not exist", archiveFile));
        }

        return new BinaryExtractor(archiveFile);
    }

    @Nonnull
    public final File unZip(final Path fileDestinationPath, final Predicate<ZipEntry> fileSelection) throws IOException {
        try (final InputStream fileInputStream = new FileInputStream(archiveFile);
             final InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             final ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream)) {

            for (ZipEntry zipEntry = zipInputStream.getNextEntry();
                 !isNull(zipEntry);
                 zipEntry = zipInputStream.getNextEntry()) {

                if (fileSelection.test(zipEntry)) {
                    Files.copy(zipInputStream, fileDestinationPath);
                    LOGGER.debug("extracted to {}", fileDestinationPath);
                    return fileDestinationPath.toFile();
                }
            }
        }

        throw new IllegalStateException("Nothing unzipped");
    }

    @Nonnull
    public final File unTarGz(final Path fileDestinationPath, final Predicate<TarArchiveEntry> fileSelection) throws IOException {
        try (final InputStream fileInputStream = new FileInputStream(archiveFile);
             final InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             final GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(bufferedInputStream);
             final TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            for (TarArchiveEntry tarEntry = tarInputStream.getNextTarEntry();
                 !isNull(tarEntry);
                 tarEntry = tarInputStream.getNextTarEntry()) {

                if (fileSelection.test(tarEntry)) {
                    Files.copy(tarInputStream, fileDestinationPath);
                    LOGGER.debug("extracted to {}", fileDestinationPath);
                    return fileDestinationPath.toFile();
                }
            }
        }

        throw new IllegalStateException("Nothing was extracted");
    }

    @Override
    public final void close() {
        if (archiveFile.exists()) {
            if (archiveFile.delete()) {
                LOGGER.debug("{} deleted", archiveFile);
            } else {
                LOGGER.warn("{} not deleted", archiveFile);
            }
        }
    }

}
