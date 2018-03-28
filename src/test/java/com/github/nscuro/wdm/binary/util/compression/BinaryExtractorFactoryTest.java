package com.github.nscuro.wdm.binary.util.compression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class BinaryExtractorFactoryTest {

    private BinaryExtractorFactory binaryExtractorFactory;

    @BeforeEach
    void beforeEach() {
        binaryExtractorFactory = new BinaryExtractorFactory();
    }

    @Nested
    class GetBinaryExtractorForArchiveFileTest {

        private File fileMock;

        @BeforeEach
        void beforeEach() {
            fileMock = mock(File.class);
        }

        @Test
        void shouldReturnZipExtractorWhenZipFileIsProvided() {
            given(fileMock.getName())
                    .willReturn("i_am_a_zip_file.zip");

            assertThat(binaryExtractorFactory.getBinaryExtractorForArchiveFile(fileMock))
                    .isInstanceOf(ZipBinaryExtractor.class);
        }

        @Test
        void shouldReturnGzipExtractorWhenGzippedFileIsProvided() {
            given(fileMock.getName())
                    .willReturn("i_am_a_gzipped_file.tar.gz");

            assertThat(binaryExtractorFactory.getBinaryExtractorForArchiveFile(fileMock))
                    .isInstanceOf(GZipTarballBinaryExtractor.class);
        }

        @Test
        void shouldThrowExceptionWhenFileDoesNotHaveAnExtension() {
            given(fileMock.getName())
                    .willReturn("i_do_not_have_an_extension");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryExtractorFactory.getBinaryExtractorForArchiveFile(fileMock));
        }

        @Test
        void shouldThrowExceptionWhenNoExtractorIsAvailableForProvidedArchiveFile() {
            given(fileMock.getName())
                    .willReturn("i_am_not_supported.txt");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> binaryExtractorFactory.getBinaryExtractorForArchiveFile(fileMock));
        }

    }

}