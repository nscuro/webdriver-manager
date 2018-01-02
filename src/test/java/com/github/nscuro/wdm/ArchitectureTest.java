package com.github.nscuro.wdm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ArchitectureTest {

    @Nested
    class GetCurrentTest {

        private final String ORIGINAL_ARCH = System.getProperty("os.arch");

        @ParameterizedTest
        @EnumSource(Architecture.class)
        void testSupported(final Architecture architecture) {
            architecture.getNames().forEach(archName -> {
                setArchName(archName);

                assertThat(Architecture.getCurrent())
                        .isEqualTo(architecture);
            });
        }

        @Test
        void testUnsupported() {
            setArchName("arm");

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(Architecture::getCurrent);
        }

        @AfterEach
        void afterEach() {
            setArchName(ORIGINAL_ARCH);
        }

        private void setArchName(final String archName) {
            System.setProperty("os.arch", archName);
        }

    }

}