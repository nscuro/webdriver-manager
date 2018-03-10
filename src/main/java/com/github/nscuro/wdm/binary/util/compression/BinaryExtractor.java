package com.github.nscuro.wdm.binary.util.compression;

import org.apache.commons.compress.archivers.ArchiveEntry;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;

public interface BinaryExtractor {

    @Nonnull
    File extractBinary(final Path binaryDestinationPath, final Predicate<ArchiveEntry> binaryEntrySelector) throws IOException;

    final class FileSelectors {

        public static Predicate<ArchiveEntry> entryIsFile() {
            return archiveEntry -> !archiveEntry.isDirectory();
        }

        public static Predicate<ArchiveEntry> entryNameStartsWithIgnoringCase(final String prefix) {
            return archiveEntry -> archiveEntry.getName().toLowerCase().startsWith(prefix.toLowerCase());
        }

    }

}
