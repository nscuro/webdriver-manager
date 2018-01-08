package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class GeckoDriverPlatformTest {

    @ParameterizedTest
    @EnumSource(GeckoDriverPlatform.class)
    void shouldDefineName(final Platform platform) {
        assertThat(platform.getName())
                .isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(GeckoDriverPlatform.class)
    void shouldDefineOs(final Platform platform) {
        assertThat(platform.getOs()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(GeckoDriverPlatform.class)
    void shouldDefineAtLeastOneArchitecture(final Platform platform) {
        assertThat(platform.getArchitectures())
                .isNotEmpty();
    }

    @Nested
    class ValueOfTest {

        @Test
        void shouldIdentifyWin32() {
            assertThat(GeckoDriverPlatform.valueOf(Os.WINDOWS, Architecture.X86))
                    .isEqualTo(GeckoDriverPlatform.WIN32);
        }

        @Test
        void shouldIdentifyWin64() {
            assertThat(GeckoDriverPlatform.valueOf(Os.WINDOWS, Architecture.X64))
                    .isEqualTo(GeckoDriverPlatform.WIN64);
        }

        @Test
        void shouldIdentifyLinux32() {
            assertThat(GeckoDriverPlatform.valueOf(Os.LINUX, Architecture.X86))
                    .isEqualTo(GeckoDriverPlatform.LINUX32);
        }

        @Test
        void shouldIdentifyLinux64() {
            assertThat(GeckoDriverPlatform.valueOf(Os.LINUX, Architecture.X64))
                    .isEqualTo(GeckoDriverPlatform.LINUX64);
        }

        @Test
        void shouldIdentifyMac64() {
            assertThat(GeckoDriverPlatform.valueOf(Os.MACOS, Architecture.X64))
                    .isEqualTo(GeckoDriverPlatform.MACOS);
        }

        @Test
        void shouldNotIdentifyMac32() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> GeckoDriverPlatform.valueOf(Os.MACOS, Architecture.X86));
        }

    }

}