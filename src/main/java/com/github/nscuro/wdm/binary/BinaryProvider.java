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
 * @since 0.1.5
 */
public interface BinaryProvider {

    boolean providesBinaryForBrowser(final Browser browser);

    @Nonnull
    Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException;

    @Nonnull
    File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException;

}
