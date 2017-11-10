package com.github.nscuro.wdm.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.isNull;

public final class CompressionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

    private CompressionUtils() {
    }

    public static Optional<File> unzipFile(final byte[] zipFileContent,
                                           final Path fileDestinationPath,
                                           final Predicate<ZipEntry> fileSelection) throws IOException {
        try (final InputStream zipFileInputStream = new ByteArrayInputStream(zipFileContent)) {

            try (final ZipInputStream zipInputStream = new ZipInputStream(zipFileInputStream)) {

                for (ZipEntry zipEntry = zipInputStream.getNextEntry(); !isNull(zipEntry); zipEntry = zipInputStream.getNextEntry()) {

                    if (fileSelection.test(zipEntry)) {
                        Files.copy(zipInputStream, fileDestinationPath);

                        LOGGER.debug("File unzipped to {}", fileDestinationPath);

                        return Optional.of(fileDestinationPath.toFile());
                    }
                }
            }
        }

        LOGGER.debug("Nothing unzipped");

        return Optional.empty();
    }

}
