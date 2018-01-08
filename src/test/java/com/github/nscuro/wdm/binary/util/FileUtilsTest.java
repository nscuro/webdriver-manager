package com.github.nscuro.wdm.binary.util;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.Os;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FileUtilsTest {

    @Nested
    class EnsureExistenceOfDirTest {

        private Path testDirPath;

        @Test
        void shouldCreateDirectoryWhenItDoesntExistAlready() {
            testDirPath = Paths.get(System.getProperty("java.io.tmpdir"), "wdm-fu-testdir");

            assumeFalse(testDirPath.toFile().exists(), "The directory must not exist for this test");

            FileUtils.ensureExistenceOfDir(testDirPath);

            assertThat(testDirPath)
                    .isDirectory();
        }

        @Test
        void shouldDoNothingWhenDirAlreadyExists() throws IOException {
            testDirPath = Files.createTempDirectory("wdm-fu-testdir");

            assumeTrue(testDirPath.toFile().exists(), "The directory must already exist for this test");

            FileUtils.ensureExistenceOfDir(testDirPath);

            assertThat(testDirPath)
                    .as("The directory must stay untouched")
                    .isDirectory();
        }

        @Test
        void shouldThrowExceptionWhenPathAlreadyExistsButIsNotADirectory() throws IOException {
            testDirPath = Files.createTempFile("wdm-fu-testfile", null);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> FileUtils.ensureExistenceOfDir(testDirPath));

            assertThat(testDirPath)
                    .as("The detected file must stay untouched")
                    .isRegularFile();
        }

        @AfterEach
        void afterEach() {
            Optional.ofNullable(testDirPath)
                    .map(Path::toFile)
                    .ifPresent(File::delete);
        }

    }

    @Nested
    class MakeFileExecutableTest {

        private File tempFile;

        @BeforeEach
        void beforeEach() throws IOException {
            tempFile = Files.createTempFile("wdm-fu-exectest", null).toFile();
        }

        @Test
        void shouldMakeNonExecutableFileExecutable() {
            assertThat(tempFile.canExecute())
                    .isFalse();

            FileUtils.makeFileExecutable(tempFile);

            assertThat(tempFile.canExecute())
                    .isTrue();
        }

        @Test
        void shouldDoNothingWhenFileIsAlreadyExecutable() {
            assumeTrue(tempFile.setExecutable(true), "File must be already executable for this test");

            FileUtils.makeFileExecutable(tempFile);

            assertThat(tempFile.canExecute())
                    .isTrue();
        }

        @Test
        void shouldThrowExceptionWhenFileDoesNotExist() {
            final File nonExistentFile = Paths.get(System.getProperty("java.io.tmpdir"), "idonotexits.rofl").toFile();

            assumeFalse(nonExistentFile.exists(), "File must not exist for this test");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> FileUtils.makeFileExecutable(nonExistentFile));
        }

        @Test
        void shouldThrowExceptionWhenInputIsDirectory() throws IOException {
            final File actuallyIsDirectory = Files.createTempDirectory("wdm-fu-exectest-dir").toFile();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> FileUtils.makeFileExecutable(actuallyIsDirectory));
        }

        @AfterEach
        void afterEach() {
            Optional.ofNullable(tempFile).ifPresent(File::delete);
        }

    }

    @Test
    void testBuildBinaryDestinationPath() {
        final Path basePath = Paths.get(System.getProperty("java.io.tmpdir"));

        assertThat(FileUtils.buildBinaryDestinationPath(Browser.CHROME, "1.1.1", Os.WINDOWS, Architecture.X64, basePath))
                .hasFileName("driver_chrome-1.1.1_windows-x64");
    }

}