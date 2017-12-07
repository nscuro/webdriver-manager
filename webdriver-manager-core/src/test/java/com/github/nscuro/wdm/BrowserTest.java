package com.github.nscuro.wdm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class BrowserTest {

    @ParameterizedTest
    @EnumSource(Browser.class)
    void shouldDefineBinarySystemProperty(final Browser browser) {
        assertThat(browser.getBinarySystemProperty())
                .isNotNull();
    }

}