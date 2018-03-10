package com.github.nscuro.wdm.binary.util.compression;

import com.github.nscuro.wdm.binary.BinaryExtractor;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class AbstractBinaryExtractorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBinaryExtractorTest.class);

    File extractedFile;

    @AfterEach
    void afterEach() {
        deleteFile(extractedFile);
    }

    @Nonnull
    static File getTestArchiveFile(final String fileName) throws IOException {
        final Path tempFilePath = Files.createTempFile(fileName, null);

        try (final InputStream inputStream = BinaryExtractor.class.getClassLoader().getResourceAsStream("test-archives/" + fileName)) {
            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFilePath.toFile();
    }

    static void deleteFile(@Nullable final File file) {
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
