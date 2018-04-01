package com.github.nscuro.wdm.binary.util.compression;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Optional;

import static java.lang.String.format;

/**
 * A factory for {@link BinaryExtractor}s.
 *
 * @since 0.2.0
 */
public class BinaryExtractorFactory {

    /**
     * Get a {@link BinaryExtractor} that is able to handle the archive format of a given {@link File}.
     * <p>
     * The determination of the archive format is based on the extension of the given {@link File},
     * thus fairly weak. Use with caution.
     *
     * @param archiveFile The archive file
     * @return A matching {@link BinaryExtractor}
     * @throws UnsupportedOperationException When no {@link BinaryExtractor} is available for the given archive format
     */
    public final BinaryExtractor getBinaryExtractorForArchiveFile(final File archiveFile) {
        final String fileExtension = Optional
                .of(archiveFile)
                .map(File::getName)
                .map(FilenameUtils::getExtension)
                .filter(StringUtils::isNotEmpty)
                .map(String::toLowerCase)
                .orElseThrow(() -> new IllegalArgumentException(format("\"%s\" does not have any file extension", archiveFile)));

        switch (fileExtension) {
            case "zip":
                return new ZipBinaryExtractor(archiveFile);
            case "gz":
                return new GZipTarballBinaryExtractor(archiveFile);
            default:
                throw new UnsupportedOperationException(format("No BinaryExtractor available for \"%s\" files", fileExtension));
        }
    }

}
