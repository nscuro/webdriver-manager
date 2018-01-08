package com.github.nscuro.wdm.binary;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public final class BinaryExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryExtractor.class);

    private final File archiveFile;

    private BinaryExtractor(final File archiveFile) {
        this.archiveFile = archiveFile;
    }

    @Nonnull
    public static BinaryExtractor fromArchiveFile(final File archiveFile) {
        requireNonNull(archiveFile, "No archiveFile provided");

        if (!archiveFile.exists()) {
            throw new IllegalArgumentException(format("Archive file %s does not exist", archiveFile));
        }

        return new BinaryExtractor(archiveFile);
    }

    @Nonnull
    public final File unZip(final Path fileDestinationPath, final Predicate<ArchiveEntry> fileSelection) throws IOException {
        try (final InputStream fileInputStream = Files.newInputStream(archiveFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
             final InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             final ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(bufferedInputStream)) {

            for (ZipArchiveEntry zipEntry = zipInputStream.getNextZipEntry();
                 !isNull(zipEntry);
                 zipEntry = zipInputStream.getNextZipEntry()) {

                if (fileSelection.test(zipEntry)) {
                    Files.copy(zipInputStream, fileDestinationPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.debug("extracted to {}", fileDestinationPath);
                    return fileDestinationPath.toFile();
                }
            }
        }

        throw new IllegalStateException("Nothing unzipped");
    }

    @Nonnull
    public final File unTarGz(final Path fileDestinationPath, final Predicate<ArchiveEntry> fileSelection) throws IOException {
        try (final InputStream fileInputStream = Files.newInputStream(archiveFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
             final InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             final GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(bufferedInputStream);
             final TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            for (TarArchiveEntry tarEntry = tarInputStream.getNextTarEntry();
                 !isNull(tarEntry);
                 tarEntry = tarInputStream.getNextTarEntry()) {

                if (fileSelection.test(tarEntry)) {
                    Files.copy(tarInputStream, fileDestinationPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.debug("extracted to {}", fileDestinationPath);
                    return fileDestinationPath.toFile();
                }
            }
        }

        throw new IllegalStateException("Nothing was extracted");
    }

    public static final class FileSelectors {

        private FileSelectors() {
        }

        public static Predicate<ArchiveEntry> entryIsFile() {
            return archiveEntry -> !archiveEntry.isDirectory();
        }

        public static Predicate<ArchiveEntry> entryNameStartsWithIgnoringCase(final String prefix) {
            return archiveEntry -> archiveEntry.getName().toLowerCase().startsWith(prefix.toLowerCase());
        }

    }

}
