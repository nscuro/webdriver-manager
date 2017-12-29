package com.github.nscuro.wdm.binary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("The binary extractor")
class BinaryExtractorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryExtractorTest.class);

    private File archiveFile;

    private File extractedFile;

    @Nested
    @DisplayName("when given a .zip archive")
    class UnZipTest {

        @BeforeEach
        void beforeEach() throws IOException {
            archiveFile = getTestArchiveFile("test-unzip.zip");
        }

        @Test
        @DisplayName("should be able to unzip a single file and delete the source archive afterwards")
        void testUnZip() throws IOException {
            try (final BinaryExtractor binaryExtractor = BinaryExtractor.fromArchiveFile(archiveFile)) {
                extractedFile = binaryExtractor.unZip(Files.createTempFile("unzipped-file", null),
                        entry -> entry.getName().equals("unzip-successful.txt"));
            }

            assertThat(extractedFile).exists();
            assertThat(archiveFile).doesNotExist();
        }

    }

    @Nested
    @DisplayName("when given a .tar.gz or .gz archive")
    class UnTarGzTest {

        @BeforeEach
        void beforeEach() throws IOException {
            archiveFile = getTestArchiveFile("test-untargz.gz");
        }

        @Test
        @DisplayName("should be able to ungzip & untar a single file and delete the source archive afterwards")
        void testUnTarGz() throws IOException {
            try (final BinaryExtractor binaryExtractor = BinaryExtractor.fromArchiveFile(archiveFile)) {
                extractedFile = binaryExtractor.unTarGz(Files.createTempFile("untargzed-file", null),
                        entry -> entry.getName().equals("untargz-successful.txt"));
            }

            assertThat(extractedFile).exists();
            assertThat(archiveFile).doesNotExist();
        }

    }

    @Test
    void shouldThrowExceptionWhenArchiveFileIsNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> BinaryExtractor.fromArchiveFile(null));
    }

    @AfterEach
    void afterEach() {
        Arrays.asList(extractedFile, archiveFile)
                .forEach(BinaryExtractorTest::deleteFile);
    }

    private static File getTestArchiveFile(final String fileName) throws IOException {
        final Path tempFilePath = Files.createTempFile(fileName, null);

        try (final InputStream inputStream = BinaryExtractor.class.getClassLoader().getResourceAsStream("test-archives/" + fileName)) {
            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFilePath.toFile();
    }

    private static void deleteFile(@Nullable final File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.delete()) {
            LOGGER.debug("{} deleted", file);
        } else {
            LOGGER.warn("{} not deleted", file);
        }
    }

}