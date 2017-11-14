package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface BinaryDownloader {

    /**
     * Indicate whether or not a given {@link Browser} is supported.
     *
     * @param browser The {@link Browser} to check support for
     * @return true when it's supported, otherwise false
     */
    boolean supportsBrowser(final Browser browser);

    /**
     * Download a specific binary version and deploy it to the given destination directory.
     *
     * @param version            The binary version to download (<strong>not</strong> the browser version)
     * @param os                 The operating system the binary must be compatible with
     * @param architecture       The architecture the binary must be compatible with
     * @param destinationDirPath Path to the directory the binary shall be deployed to
     * @return A {@link File} handle of the downloaded binary
     * @throws IOException When downloading the binary failed
     */
    @Nonnull
    File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException;

    /**
     * @param os
     * @param architecture
     * @param destinationDirPath
     * @return
     * @throws IOException
     */
    @Nonnull
    File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException;

}
