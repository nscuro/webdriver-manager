package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.Browser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebDriverFactoryConfigTest {

    private WebDriverFactoryConfig config;

    @BeforeEach
    void beforeEach() {
        config = new WebDriverFactoryConfig();
    }

    @Nested
    class BinaryVersionForBrowserTest {

        @Test
        void shouldReturnDesiredVersionWhenSet() {
            config.setBinaryVersionForBrowser(Browser.CHROME, "1.1.1");

            assertThat(config.getBinaryVersions()).containsEntry(Browser.CHROME, "1.1.1");
            assertThat(config.getBinaryVersionForBrowser(Browser.CHROME)).contains("1.1.1");
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoVersionIsSet() {
            assertThat(config.getBinaryVersions()).isEmpty();
            assertThat(config.getBinaryVersionForBrowser(Browser.CHROME)).isNotPresent();
        }

    }

}