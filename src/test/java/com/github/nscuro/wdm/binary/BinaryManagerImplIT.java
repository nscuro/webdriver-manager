package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryManagerImplIT {

    private BinaryManager binaryManager;

    private File downloadedFile;

    @BeforeEach
    void beforeEach() {
        binaryManager = BinaryManager.createDefault();
    }

    /**
     * Test the basic success case for:
     * <pre>
     *     - Downloading of latest binary
     *     - Registration of the binary
     *     - Identification of the binary
     * </pre>
     * <p>
     * We're using {@link Browser#CHROME} here because Google Cloud Storage
     * does not limit the amount of requests being performed against their
     * API as GitHub does. So this will hopefully result in better stability.
     */
    @Test
    void shouldDownloadAndRegisterLatestBinary() throws IOException {
        downloadedFile = binaryManager.getLatestWebDriverBinary(Browser.CHROME, Os.WINDOWS, Architecture.X64);

        assertThat(downloadedFile)
                .as("downloaded file should exist")
                .exists();

        assertThat(downloadedFile.canExecute())
                .as("downloaded file must be executable")
                .isTrue();

        binaryManager.registerWebDriverBinary(Browser.CHROME, downloadedFile);

        //noinspection ConstantConditions
        assertThat(System.getProperty(Browser.CHROME.getBinarySystemProperty().get()))
                .as("should correctly register downloaded binary")
                .isEqualTo(downloadedFile.getAbsolutePath());

        assertThat(binaryManager.getLocalWebDriverBinaries())
                .as("should identify downloaded file as webDriver binary")
                .contains(downloadedFile);
    }

    @AfterEach
    void afterEach() {
        //noinspection ResultOfMethodCallIgnored
        Optional
                .ofNullable(downloadedFile)
                .ifPresent(File::delete);
    }

}