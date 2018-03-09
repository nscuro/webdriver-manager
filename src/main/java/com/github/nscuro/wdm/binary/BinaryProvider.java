package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface BinaryProvider {

    @Nonnull
    Optional<String> getLatestBinaryVersion(final Os os, final Architecture architecture) throws IOException;

    @Nonnull
    File download(final String version, final Os os, final Architecture architecture, final Path binaryDestinationPath) throws IOException;

}
