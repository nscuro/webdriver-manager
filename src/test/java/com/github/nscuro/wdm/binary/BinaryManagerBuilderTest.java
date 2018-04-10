package com.github.nscuro.wdm.binary;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BinaryManagerBuilderTest {

    @Test
    void shouldThrowExceptionWhenHttpClientIsNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> BinaryManager.builder().httpClient(null).defaultBinaryDestinationDir().build());
    }

    @Test
    void shouldThrowExceptionWhenBinaryDestinationDirIsNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> BinaryManager.builder().defaultHttpClient().binaryDestinationDir(null).build());
    }

}