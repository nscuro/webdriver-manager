package com.github.nscuro.wdm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

class BrowserTest {

    @ParameterizedTest
    @EnumSource(Browser.class)
    void shouldDefineBinarySystemProperty(final Browser browser) {
        assertThat(browser.getBinarySystemProperty())
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(Browser.class)
    void shouldDefineValidWebDriverClassName(final Browser browser) throws ClassNotFoundException {
        assertThat(WebDriver.class)
                .isAssignableFrom(Class.forName(browser.getWebDriverClassName()));
    }

}