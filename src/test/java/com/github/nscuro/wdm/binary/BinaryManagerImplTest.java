package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Browser;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

class BinaryManagerImplTest {

    private BinaryManagerImpl binaryManager;

    @BeforeEach
    void beforeEach() {
        binaryManager = new BinaryManagerImpl(java.util.Collections.emptySet());
    }

    @Nested
    class RegisterBinaryTest {

        @Test
        void shouldThrowExceptionWhenFileHandlePointsToNonExistentFile() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.registerBinary(new File("idonotexist"), Browser.CHROME));
        }

        @Test
        void shouldThrowExceptionWhenFileHandlePointsToDirectory() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> binaryManager.registerBinary(Paths.get(System.getProperty("java.io.tmpdir")).toFile(), Browser.CHROME));
        }

    }

}