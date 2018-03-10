package com.github.nscuro.wdm.binary.util.compression;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Optional;

import static java.lang.String.format;

public class BinaryExtractorFactory {

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
