package com.github.nscuro.wdm;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BrowserTest {

    @ParameterizedTest
    @EnumSource(Browser.class)
    void shouldDefineBinarySystemPropertyWhenBinaryIsRequired(final Browser browser) {
        assumeTrue(browser.doesRequireBinary());

        assertThat(browser.getBinarySystemProperty()).isPresent();
    }

    @ParameterizedTest
    @EnumSource(Browser.class)
    void shouldNotDefineBinarySystemPropertyWhenBinaryIsNotRequired(final Browser browser) {
        assumeFalse(browser.doesRequireBinary());

        assertThat(browser.getBinarySystemProperty()).isNotPresent();
    }

    @ParameterizedTest
    @EnumSource(Browser.class)
    void shouldDefineValidWebDriverClassName(final Browser browser) throws ClassNotFoundException {
        assertThat(WebDriver.class)
                .isAssignableFrom(Class.forName(browser.getWebDriverClassName()));
    }

    @Nested
    class ByNameTest {

        @ParameterizedTest
        @EnumSource(Browser.class)
        void shouldReturnBrowserWhenNameIsKnown(final Browser browser) {
            browser.getNames().forEach(name -> assertThat(Browser.byName(name)).isEqualTo(browser));
        }

        @Test
        void shouldThrowExceptionWhenNameIsNotKnown() {
            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> Browser.byName("unknown"));
        }

    }

}