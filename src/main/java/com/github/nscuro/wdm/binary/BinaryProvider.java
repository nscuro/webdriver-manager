package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Provider for WebDriver binaries.
 *
 * A {@link BinaryProvider} should provide binaries for only one {@link Browser}.
 *
 * @since 0.2.0
 */
public interface BinaryProvider {

    /**
     * Indicate whether or not binaries can be provided for a given {@link Browser}.
     *
     * @param browser The {@link Browser} to check the support for
     * @return true when binaries can be provided, otherwise false
     */
    boolean providesBinaryForBrowser(final Browser browser);

    /**
     * Get the latest available binary version for a given {@link Os} and {@link Architecture}.
     *
     * @param os           The {@link Os} to get the latest binary version for
     * @param architecture The {@link Architecture} to get the latest binary version for
     * @return A version that is available for the desired platform or {@link Optional#empty()},
     *         when there is no version available
     * @throws IOException In case of a networking error
     */
    @Nonnull
    Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException;

    /**
     * Download the binary in a given version for a given {@link Os} and {@link Architecture}.
     *
     * @param version               The desired version
     * @param os                    The {@link Os} to get the binary for
     * @param architecture          The {@link Architecture} to get the binary for
     * @param binaryDestinationPath The destination {@link Path} the binary shall be deployed to
     * @return The desired binary file
     * @throws IOException In case of a networking error
     */
    @Nonnull
    File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException;

}
