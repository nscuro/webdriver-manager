package com.github.nscuro.wdm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OsTest {

    @Nested
    class GetCurrentTest {

        private final String ORIGINAL_OS = System.getProperty("os.name");

        @ParameterizedTest(name = "[{index}] osName={0}")
        @ValueSource(strings = {
                "Windows 2000",
                "Windows XP",
                "Windows Vista",
                "Windows 7",
                "Windows 8",
                "Windows 8.1",
                "Windows 10"
        })
        void testForWindows(final String osName) {
            setOsName(osName);

            assertThat(Os.getCurrent())
                    .as("Windows must be correctly detected")
                    .isEqualTo(Os.WINDOWS);
        }

        @ParameterizedTest(name = "[{index}] osName={0}")
        @ValueSource(strings = {
                // TODO
                "linux"
        })
        void testForLinux(final String osName) {
            setOsName(osName);

            assertThat(Os.getCurrent())
                    .as("Linux must be correctly detected")
                    .isEqualTo(Os.LINUX);
        }

        @ParameterizedTest(name = "[{index}] osName={0}")
        @ValueSource(strings = {
                // TODO
                "mac"
        })
        void testForMacOs(final String osName) {
            setOsName(osName);

            assertThat(Os.getCurrent())
                    .as("MacOS must be correctly detected")
                    .isEqualTo(Os.MACOS);
        }

        @AfterEach
        void afterEach() {
            setOsName(ORIGINAL_OS);
        }

        private void setOsName(final String osName) {
            System.setProperty("os.name", osName);
        }

    }

}
