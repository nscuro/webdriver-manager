package com.github.nscuro.wdm.binary.util;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

import static java.lang.String.format;

/**
 * File related utility methods.
 */
public final class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    /**
     * Ensure that the directory at a given {@link Path} exists.
     *
     * @param dirPath The {@link Path} to ensure the existence of
     * @throws IllegalStateException When given path exists, but does not point to a directory
     */
    public static void ensureExistenceOfDir(final Path dirPath) {
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
     * Make a given file executable.
     *
     * @param file The file to be made executable
     * @throws IllegalArgumentException When the given file does not exist or is a directory
     */
    public static void makeFileExecutable(final File file) {
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException(format("Cannot make file executable: %s does not exist or is a directory", file));
        } else if (!file.canExecute()) {
            if (!file.setExecutable(true)) {
                LOGGER.warn("{} was not made executable", file);
            } else {
                LOGGER.debug("{} was made executable", file);
            }
        }
    }

    /**
     * Build a destination {@link Path} to deploy a binary with the given attributes to.
     *
     * @param browser      The {@link Browser} the binary is for
     * @param version      The binary's version
     * @param os           The {@link Os} the binary was compiled for
     * @param architecture The {@link Architecture} the binary was compiled for
     * @param basePath     Base {@link Path} of the destination
     * @return The destination {@link Path}
     */
    @Nonnull
    public static Path buildBinaryDestinationPath(final Browser browser, final String version,
                                                  final Os os, final Architecture architecture,
                                                  final Path basePath) {
        final String binaryFileName = format("driver_%s-%s_%s-%s", browser, version, os, architecture).toLowerCase();

        return basePath.resolve(binaryFileName);
    }

}
