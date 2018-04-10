package com.github.nscuro.wdm.binary.util.compression;

import org.apache.commons.compress.archivers.ArchiveEntry;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Extractor for WebDriver binaries.
 *
 * @since 0.2.0
 */
public interface BinaryExtractor {

    /**
     * Extract a file matching a given {@link Predicate} from the archive to a given {@link Path}.
     *
     * @param binaryDestinationPath Path to where the file shall be extracted to
     * @param binaryEntrySelector   Selector for the file to extract
     * @return The extracted {@link File}
     * @throws IOException            When the extraction failed
     * @throws NoSuchElementException When no file in the archive was matched by the given {@link Predicate}
     */
    @Nonnull
    File extractBinary(final Path binaryDestinationPath, final Predicate<ArchiveEntry> binaryEntrySelector) throws IOException;

    /**
     * Common selectors used for {@link BinaryExtractor#extractBinary(Path, Predicate)}.
     */
    final class FileSelectors {

        public static Predicate<ArchiveEntry> entryIsFile() {
            return archiveEntry -> !archiveEntry.isDirectory();
        }

        public static Predicate<ArchiveEntry> entryNameStartsWithIgnoringCase(final String prefix) {
            return archiveEntry -> archiveEntry.getName().toLowerCase().startsWith(prefix.toLowerCase());
        }

    }

}
