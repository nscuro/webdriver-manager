package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ChromeDriverPlatformTest {

    @Nested
    class FromTest {

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