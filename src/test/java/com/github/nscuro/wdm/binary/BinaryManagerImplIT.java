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
class BinaryManagerImplIT {

    private BinaryManager binaryManager;

    private File downloadedFile;

    @BeforeEach
    void beforeEach() {
        binaryManager = BinaryManager.createDefault();
    }

    @Test
    void shouldDownloadAndRegisterLatestBinary() throws IOException {
        downloadedFile = binaryManager.getLatestWebDriverBinary(Browser.CHROME, Os.WINDOWS, Architecture.X64);

        assertThat(downloadedFile)
                .as("downloaded file should exist")
                .exists();

        assertThat(downloadedFile.canExecute())
                .as("downloaded file must be executable")
                .isTrue();

        assertThat(downloadedFile.getName())
                .as("downloaded file's name should contain the browser name")
                .containsIgnoringCase(Browser.CHROME.name())
                .as("downloaded file's name should contain the OS name")
                .containsIgnoringCase(Os.WINDOWS.name())
                .as("downloaded file's name should contain the architectures name")
                .containsIgnoringCase(Architecture.X64.name());

        binaryManager.registerWebDriverBinary(Browser.CHROME, downloadedFile);

        //noinspection ConstantConditions
        assertThat(System.getProperty(Browser.CHROME.getBinarySystemProperty().get()))
                .as("should correctly register downloaded binary")
                .isEqualTo(downloadedFile.getAbsolutePath());

        assertThat(binaryManager.getLocalWebDriverBinaries())
                .as("should identify downloaded file as webDriver binary")
                .contains(downloadedFile);
    }

    @Test
    void shouldDownloadAndRegisterSpecificBinaryVersion() throws IOException {
        downloadedFile = binaryManager.getWebDriverBinary(Browser.CHROME, "2.34", Os.MACOS, Architecture.X64);

        assertThat(downloadedFile)
                .as("downloaded file should exist")
                .exists();

        assertThat(downloadedFile.canExecute())
                .as("downloaded file must be executable")
                .isTrue();

        assertThat(downloadedFile.getName())
                .as("downloaded file's name should contain the browser name")
                .containsIgnoringCase(Browser.CHROME.name())
                .as("downloaded file's name should contain the OS name")
                .containsIgnoringCase(Os.MACOS.name())
                .as("downloaded file's name should contain the architectures name")
                .containsIgnoringCase(Architecture.X64.name())
                .as("downloaded file's name should contain the version")
                .contains("2.34");

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