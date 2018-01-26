package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.binary.BinaryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

class LocalWebDriverFactoryTest {

    private LocalWebDriverFactory webDriverFactory;

    @Nested
    class CreateWebDriverTestWithBinaryManager {

        private BinaryManager binaryManager;

        @BeforeEach
        void beforeEach() {
            binaryManager = mock(BinaryManager.class);

            webDriverFactory = new LocalWebDriverFactory(binaryManager);
        }

        @Test
        void shouldThrowExceptionWhenNoBrowserNameWasProvided() {
            final MutableCapabilities desiredCapabilities = new MutableCapabilities();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> webDriverFactory.createWebDriver(desiredCapabilities));
        }

    }

    @Nested
    class CreateWebDriverTestWithoutBinaryManager {

        @BeforeEach
        void beforeEach() {
            webDriverFactory = new LocalWebDriverFactory();
        }

        @Test
        void shouldThrowExceptionWhenRequestingBrowserThatRequiresBinaryWhenBinaryManagerIsNull() {
            assertThatExceptionOfType(WebDriverFactoryException.class)
                    .isThrownBy(() -> webDriverFactory.createWebDriver(new ChromeOptions()))
                    .withMessageContaining(BinaryManager.class.getSimpleName());
        }

    }

}