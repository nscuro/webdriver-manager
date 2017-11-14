package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

import static java.lang.String.format;

public final class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * @param dirPath
     */
    static void ensureExistenceOfDir(final Path dirPath) {
        final File dir = dirPath.toFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                LOGGER.warn("Did not create directory at {}", dir);
            } else {
                LOGGER.debug("Created directory at {}", dir);
            }
        } else if (!dir.isDirectory()) {
            throw new IllegalStateException(format("%s already exists, but is not a directory", dir));
        } else {
            LOGGER.debug("Directory at {} already exists", dir);
        }
    }

    /**
     * @param browser
     * @param version
     * @param os
     * @param architecture
     * @param basePath
     * @return
     */
    @Nonnull
    public static Path buildBinaryDestinationPath(final Browser browser, final String version, final Os os, final Architecture architecture, final Path basePath) {
        final String binaryFileName = format("driver_%s-%s_%s-%s", browser, version, os, architecture).toLowerCase();

        return basePath.resolve(binaryFileName);
    }

}
