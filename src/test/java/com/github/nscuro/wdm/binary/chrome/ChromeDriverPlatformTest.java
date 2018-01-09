package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ChromeDriverPlatformTest {

    @ParameterizedTest
    @EnumSource(ChromeDriverPlatform.class)
    void shouldDefineName(final Platform platform) {
        assertThat(platform.getName())
                .isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(ChromeDriverPlatform.class)
    void shouldDefineOs(final Platform platform) {
        assertThat(platform.getOs()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(ChromeDriverPlatform.class)
    void shouldDefineAtLeastOneArchitecture(final Platform platform) {
        assertThat(platform.getArchitectures())
                .isNotEmpty();
    }

    @Nested
    class ValueOfTest {

        @Test
        void testWin32() {
            assertThat(ChromeDriverPlatform.valueOf(Os.WINDOWS, Architecture.X86))
                    .isEqualTo(ChromeDriverPlatform.WIN32);

            assertThat(ChromeDriverPlatform.valueOf(Os.WINDOWS, Architecture.X64))
                    .isEqualTo(ChromeDriverPlatform.WIN32);
        }

        @Test
        void testLinux64() {
            assertThat(ChromeDriverPlatform.valueOf(Os.LINUX, Architecture.X64))
                    .isEqualTo(ChromeDriverPlatform.LINUX64);
        }

        @Test
        void testLinux32() {
            assertThat(ChromeDriverPlatform.valueOf(Os.LINUX, Architecture.X86))
                    .isEqualTo(ChromeDriverPlatform.LINUX32);
        }

        @Test
        void testMac64Positive() {
            assertThat(ChromeDriverPlatform.valueOf(Os.MACOS, Architecture.X64))
                    .isEqualTo(ChromeDriverPlatform.MAC64);
        }

        @Test
        void testMac64Negative() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> ChromeDriverPlatform.valueOf(Os.MACOS, Architecture.X86));
        }

    }

}