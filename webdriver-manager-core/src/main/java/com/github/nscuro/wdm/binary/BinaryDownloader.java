package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface BinaryDownloader {

    boolean supportsBrowser(final Browser browser);

    @Nonnull
    File download(final String version, final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException;

    @Nonnull
    File downloadLatest(final Os os, final Architecture architecture, final Path destinationDirPath) throws IOException;

    @Nonnull
    default File download(final String version, final Path destinationDirPath) throws IOException {
        return download(version, Os.getCurrent(), Architecture.getCurrent(), destinationDirPath);
    }

    @Nonnull
    default File downloadLatest(final Path destinationDirPath) throws IOException {
        return downloadLatest(Os.getCurrent(), Architecture.getCurrent(), destinationDirPath);
    }

}
