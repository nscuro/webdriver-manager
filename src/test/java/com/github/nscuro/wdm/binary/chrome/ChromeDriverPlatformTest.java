package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChromeDriverPlatformTest {

    @ParameterizedTest(name = "[{index}] platform={0}")
    @EnumSource(ChromeDriverPlatform.class)
    void shouldDefineName(final Platform platform) {
        assertThat(platform.getName())
                .isNotBlank();
    }

    @ParameterizedTest(name = "[{index}] platform={0}")
    @EnumSource(ChromeDriverPlatform.class)
    void shouldDefineOs(final Platform platform) {
        assertThat(platform.getOs()).isNotNull();
    }

    @ParameterizedTest(name = "[{index}] platform={0}")
    @EnumSource(ChromeDriverPlatform.class)
    void shouldDefineAtLeastOneArchitecture(final Platform platform) {
        assertThat(platform.getArchitectures())
                .isNotEmpty();
    }

    @ParameterizedTest(name = "[{index}] os={0} architecture={1} expectedPlatform={2}")
    @MethodSource("provideSupportedValueOfArguments")
    void valueOfShouldReturnCorrectPlatformForSupportedOsAndArchitecture(final Os os,
                                                                         final Architecture architecture,
                                                                         final ChromeDriverPlatform expectedPlatform) {
        assertThat(ChromeDriverPlatform.valueOf(os, architecture))
                .isPresent()
                .hasValue(expectedPlatform);
    }

    @ParameterizedTest(name = "[{index}] os={0} architecture={1}")
    @MethodSource("provideUnsupportedValueOfArguments")
    void valueOfShouldReturnEmptyOptionalForUnsupportedOsAndArchitecture(final Os os, final Architecture architecture) {
        assertThat(ChromeDriverPlatform.valueOf(os, architecture))
                .isNotPresent();
    }

    private static Stream<Arguments> provideSupportedValueOfArguments() {
        return Stream.of(
                Arguments.of(Os.WINDOWS, Architecture.X86, ChromeDriverPlatform.WIN32),
                Arguments.of(Os.WINDOWS, Architecture.X64, ChromeDriverPlatform.WIN32),
                Arguments.of(Os.LINUX, Architecture.X86, ChromeDriverPlatform.LINUX32),
                Arguments.of(Os.LINUX, Architecture.X64, ChromeDriverPlatform.LINUX64),
                Arguments.of(Os.MACOS, Architecture.X64, ChromeDriverPlatform.MAC64)
        );
    }

    private static Stream<Arguments> provideUnsupportedValueOfArguments() {
        return Stream.of(Arguments.of(Os.MACOS, Architecture.X86));
    }

}