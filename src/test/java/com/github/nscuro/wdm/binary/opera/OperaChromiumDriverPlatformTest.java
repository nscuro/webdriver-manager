package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OperaChromiumDriverPlatformTest {

    @ParameterizedTest
    @EnumSource(OperaChromiumDriverPlatform.class)
    void shouldDefineName(final Platform platform) {
        assertThat(platform.getName())
                .isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(OperaChromiumDriverPlatform.class)
    void shouldDefineOs(final Platform platform) {
        assertThat(platform.getOs())
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(OperaChromiumDriverPlatform.class)
    void shouldDefineAtLeastOneArchitecture(final Platform platform) {
        assertThat(platform.getArchitectures())
                .isNotEmpty();
    }

    @Nested
    class ValueOfTest {

        @Test
        void shouldIdentifyWin32() {
            assertThat(OperaChromiumDriverPlatform.valueOf(Os.WINDOWS, Architecture.X86))
                    .hasValue(OperaChromiumDriverPlatform.WIN32);
        }

        @Test
        void shouldIdentifyWin64() {
            assertThat(OperaChromiumDriverPlatform.valueOf(Os.WINDOWS, Architecture.X64))
                    .hasValue(OperaChromiumDriverPlatform.WIN64);
        }

        @Test
        void shouldIdentifyLinux32() {
            assertThat(OperaChromiumDriverPlatform.valueOf(Os.LINUX, Architecture.X86))
                    .hasValue(OperaChromiumDriverPlatform.LINUX32);
        }

        @Test
        void shouldIdentifyLinux64() {
            assertThat(OperaChromiumDriverPlatform.valueOf(Os.LINUX, Architecture.X64))
                    .hasValue(OperaChromiumDriverPlatform.LINUX64);
        }

        @Test
        void shouldIdentifyMac64() {
            assertThat(OperaChromiumDriverPlatform.valueOf(Os.MACOS, Architecture.X64))
                    .hasValue(OperaChromiumDriverPlatform.MAC64);
        }

        @Test
        void shouldNotIdentifyMac32() {
            assertThat(OperaChromiumDriverPlatform.valueOf(Os.MACOS, Architecture.X86))
                    .isNotPresent();
        }

    }

}