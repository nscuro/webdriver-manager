package com.github.nscuro.wdm.binary.util.compression;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ZipBinaryExtractorTest extends AbstractBinaryExtractorTest {

    private BinaryExtractor binaryExtractor;

    private File fileMock;

    @BeforeEach
    void beforeEach() {
        fileMock = mock(File.class);
    }

    @Nested
    class ExtractBinaryTest {

        private File archiveFile;

        @Test
        void shouldUnzipMatchingFileFromArchive() throws IOException {
            archiveFile = getTestArchiveFile("test-unzip.zip");

            binaryExtractor = new ZipBinaryExtractor(archiveFile);

            extractedFile = binaryExtractor
                    .extractBinary(Files.createTempFile("unzipped-file", null),
                            entry -> entry.getName().equals("unzip-successful.txt"));

            assertThat(extractedFile).exists();
            assertThat(archiveFile).doesNotExist();
        }

        @Test
        void shouldThrowExceptionWhenArchiveFileDoesNotExist() {
            given(fileMock.exists())
                    .willReturn(false);

            binaryExtractor = new ZipBinaryExtractor(fileMock);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> binaryExtractor.extractBinary(mock(Path.class), entry -> false));
        }

        @Test
        void shouldThrowExceptionWhenArchiveFileIsNotReadable() {
            given(fileMock.exists())
                    .willReturn(true);

            given(fileMock.canRead())
                    .willReturn(false);

            binaryExtractor = new ZipBinaryExtractor(fileMock);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> binaryExtractor.extractBinary(mock(Path.class), entry -> false));
        }

        @Test
        void shouldThrowExceptionWhenNothingWasExtracted() throws IOException {
            archiveFile = getTestArchiveFile("test-unzip.zip");

            binaryExtractor = new ZipBinaryExtractor(archiveFile);

            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> extractedFile = binaryExtractor
                            .extractBinary(Files.createTempFile("unzipped-file", null),
                                    entry -> entry.getName().equals("doesNotExist.txt")));
        }

        @AfterEach
        void afterEach() {
            deleteFile(archiveFile);
        }

    }

}