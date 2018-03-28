package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BinaryManagerImplTest {

    private Path binaryDestinationDirPathMock;

    private File binaryDestinationDirFileMock;

    private BinaryProvider binaryProviderMock;

    private BinaryManagerImpl binaryManager;

    @BeforeEach
    void beforeEach() {
        binaryDestinationDirPathMock = mock(Path.class);

        binaryDestinationDirFileMock = mock(File.class);

        // Prepare the binaryDestinationDirFileMock so that the
        // path validation will succeed per default. This is dirty
        // and not good, but I don't have a better solution for this...
        given(binaryDestinationDirFileMock.exists())
                .willReturn(true);

        given(binaryDestinationDirFileMock.isDirectory())
                .willReturn(true);

        given(binaryDestinationDirFileMock.canWrite())
                .willReturn(true);

        given(binaryDestinationDirPathMock.toFile())
                .willReturn(binaryDestinationDirFileMock);

        binaryProviderMock = mock(BinaryProvider.class);

        binaryManager = new BinaryManagerImpl(binaryDestinationDirPathMock, singleton(binaryProviderMock));
    }

    @Nested
    class GetWebDriverBinaryTest {

        private Path binaryDestinationFilePathMock;

        private File webDriverBinaryFileMock;

        @BeforeEach
        void beforeEach() {
            binaryDestinationFilePathMock = mock(Path.class);

            webDriverBinaryFileMock = mock(File.class);

            given(binaryDestinationFilePathMock.toFile())
                    .willReturn(webDriverBinaryFileMock);

            given(binaryDestinationDirPathMock.resolve(any(String.class)))
                    .willReturn(binaryDestinationFilePathMock);
        }

        @Test
        void shouldNotDetermineLatestVersionAndNotDownloadAnythingWhenSpecificVersionIsProvidedAndBinaryExistsLocally() throws IOException {
            given(binaryProviderMock.providesBinaryForBrowser(any(Browser.class)))
                    .willReturn(true);

            given(webDriverBinaryFileMock.exists())
                    .willReturn(true);

            assertThat(binaryManager.getWebDriverBinary(Browser.CHROME, "1.0"))
                    .isEqualTo(webDriverBinaryFileMock);

            verify(binaryProviderMock, times(0)).getLatestBinaryVersion(any(Os.class), any(Architecture.class));
            verify(webDriverBinaryFileMock).setExecutable(eq(true));
        }

        @Test
        void shouldDetermineLatestVersionAndDownloadItWhenNoSpecificVersionIsProvidedAndBinaryDoesNotExistLocally() throws IOException {
            given(binaryProviderMock.providesBinaryForBrowser(any(Browser.class)))
                    .willReturn(true);

            given(binaryProviderMock.getLatestBinaryVersion(any(Os.class), any(Architecture.class)))
                    .willReturn(Optional.of("1.0"));

            given(webDriverBinaryFileMock.exists())
                    .willReturn(false);

            final File downloadedFileMock = mock(File.class);

            given(binaryProviderMock.download(eq("1.0"), any(Os.class), any(Architecture.class), eq(binaryDestinationFilePathMock)))
                    .willReturn(downloadedFileMock);

            assertThat(binaryManager.getLatestWebDriverBinary(Browser.CHROME))
                    .isEqualTo(downloadedFileMock);

            verify(downloadedFileMock).setExecutable(eq(true));
        }

        @Test
        void shouldThrowExceptionWhenNoBinaryProviderForRequestedBrowserExists() {
            given(binaryProviderMock.providesBinaryForBrowser(any(Browser.class)))
                    .willReturn(false);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryManager.getLatestWebDriverBinary(Browser.CHROME));
        }

        @Test
        void shouldThrowExceptionWhenLatestBinaryVersionCannotBeDetermined() throws IOException {
            given(binaryProviderMock.providesBinaryForBrowser(any(Browser.class)))
                    .willReturn(true);

            given(binaryProviderMock.getLatestBinaryVersion(any(Os.class), any(Architecture.class)))
                    .willReturn(Optional.empty());

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> binaryManager.getLatestWebDriverBinary(Browser.CHROME, Os.WINDOWS, Architecture.X64));
        }

    }

    @Nested
    class RegisterWebDriverBinaryTest {

        private File binaryFileMock;

        @BeforeEach
        void beforeEach() {
            binaryFileMock = mock(File.class);
        }

        @Test
        void shouldThrowExceptionWhenFileDoesNotExist() {
            given(binaryFileMock.exists())
                    .willReturn(false);

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.registerWebDriverBinary(Browser.CHROME, binaryFileMock))
                    .withMessageContaining("does not exist");
        }

        @Test
        void shouldThrowExceptionWhenFileIsDirectory() {
            given(binaryFileMock.exists())
                    .willReturn(true);

            given(binaryFileMock.isDirectory())
                    .willReturn(true);

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.registerWebDriverBinary(Browser.CHROME, binaryFileMock))
                    .withMessageContaining("is a directory");
        }

        @Test
        void shouldThrowExceptionWhenBrowserDoesNotDefineSystemProperty() {
            given(binaryFileMock.exists())
                    .willReturn(true);

            given(binaryFileMock.isDirectory())
                    .willReturn(false);

            // Can't mock Browser as it's an Enum class (effectively final)
            final Browser browserWithoutSystemProperty = Arrays
                    .stream(Browser.values())
                    .filter(browser -> !browser.getBinarySystemProperty().isPresent())
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("No Browser found that does not provide a system property"));

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryManager.registerWebDriverBinary(browserWithoutSystemProperty, binaryFileMock));
        }

    }

    @Nested
    class GetLocalWebDriverBinariesTest {

        @Test
        void shouldReturnListOfExistingWebDriverBinaries() {
            final File fileMock = mock(File.class);

            given(fileMock.getName())
                    .willReturn(BinaryManagerImpl.WEB_DRIVER_BINARY_PREFIX + "_some-binary-file");

            given(binaryDestinationDirFileMock.listFiles(any(FileFilter.class)))
                    .willReturn(new File[]{fileMock});

            assertThat(binaryManager.getLocalWebDriverBinaries())
                    .containsOnly(fileMock);
        }

        @Test
        void shouldReturnEmptyListWhenDirectoryIsEmpty() {
            given(binaryDestinationDirFileMock.listFiles(any(FileFilter.class)))
                    .willReturn(new File[]{});

            assertThat(binaryManager.getLocalWebDriverBinaries())
                    .isEmpty();
        }

    }

    @Nested
    class ValidateAndPrepareBinaryDestinationDirPathTest {

        @Test
        void shouldReturnInputPathWhenFileExistsIsDirectoryAndHasWritePermissions() {
            given(binaryDestinationDirFileMock.exists())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.isDirectory())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.canWrite())
                    .willReturn(true);

            assertThat(binaryManager.validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPathMock))
                    .isEqualTo(binaryDestinationDirPathMock);
        }

        @Test
        void shouldThrowExceptionWhenFileExistsButIsNoDirectory() {
            given(binaryDestinationDirFileMock.exists())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.isDirectory())
                    .willReturn(false);

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPathMock))
                    .withMessageContaining("not a directory");
        }

        @Test
        void shouldThrowExceptionWhenFileExistsAndIsDirectoryButHasNoWritePermissions() {
            given(binaryDestinationDirFileMock.exists())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.isDirectory())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.canWrite())
                    .willReturn(false);

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPathMock))
                    .withMessageContaining("no write permissions");
        }

        @Test
        void shouldReturnInputPathWhenFileDoesNotExistsParentHasWritePermissionsAndMkdirSucceeded() {
            given(binaryDestinationDirFileMock.exists())
                    .willReturn(false);

            final File parentFileMock = mock(File.class);

            given(parentFileMock.canWrite())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.getParentFile())
                    .willReturn(parentFileMock);

            given(binaryDestinationDirFileMock.mkdirs())
                    .willReturn(true);

            assertThat(binaryManager.validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPathMock))
                    .isEqualTo(binaryDestinationDirPathMock);
        }

        @Test
        void shouldThrowExceptionWhenFileDoesNotExistAndParentHasNoWritePermissions() {
            given(binaryDestinationDirFileMock.exists())
                    .willReturn(false);

            final File parentFileMock = mock(File.class);

            given(parentFileMock.canWrite())
                    .willReturn(false);

            given(binaryDestinationDirFileMock.getParentFile())
                    .willReturn(parentFileMock);

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPathMock))
                    .withMessageContaining("no write permissions in parent");
        }

        @Test
        void shouldThrowExceptionWhenFileDoesNotExistAndMkdirFailed() {
            given(binaryDestinationDirFileMock.exists())
                    .willReturn(false);

            final File parentFileMock = mock(File.class);

            given(parentFileMock.canWrite())
                    .willReturn(true);

            given(binaryDestinationDirFileMock.getParentFile())
                    .willReturn(parentFileMock);

            given(binaryDestinationDirFileMock.mkdirs())
                    .willReturn(false);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> binaryManager.validateAndPrepareBinaryDestinationDirPath(binaryDestinationDirPathMock))
                    .withMessageContaining("has not been created");
        }

    }

    @Nested
    class IsWebDriverBinaryTest {

        private File inputFileMock;

        @BeforeEach
        void beforeEach() {
            inputFileMock = mock(File.class);
        }

        @Test
        void shouldReturnFalseWhenInputIsNotAFile() {
            given(inputFileMock.isFile())
                    .willReturn(false);

            assertThat(binaryManager.isWebDriverBinary(inputFileMock)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenInputNameDoesNotMatchTheWebDriverBinaryScheme() {
            given(inputFileMock.isFile())
                    .willReturn(true);

            given(inputFileMock.getName())
                    .willReturn("notAWebDriverBinary");

            assertThat(binaryManager.isWebDriverBinary(inputFileMock)).isFalse();
        }

        @Test
        void shouldReturnTrueWhenInputIsAFileAndNamesMatchesTheWebDriverBinaryScheme() {
            given(inputFileMock.isFile())
                    .willReturn(true);

            given(inputFileMock.getName())
                    .willReturn(BinaryManagerImpl.WEB_DRIVER_BINARY_PREFIX + "_someBrowser");

            assertThat(binaryManager.isWebDriverBinary(inputFileMock)).isTrue();
        }

    }

}