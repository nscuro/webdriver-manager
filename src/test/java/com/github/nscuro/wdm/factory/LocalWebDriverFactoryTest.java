package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.binary.BinaryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

@DisplayName("The local WebDriver factory")
class LocalWebDriverFactoryTest {

    private LocalWebDriverFactory webDriverFactory;

    @Nested
    @DisplayName("when creating WebDriver instances with BinaryManager")
    class CreateWebDriverTestWithBinaryManager {

        private BinaryManager binaryManager;

        @BeforeEach
        void beforeEach() {
            binaryManager = mock(BinaryManager.class);

            webDriverFactory = new LocalWebDriverFactory(binaryManager);
        }

        @Test
        @DisplayName("should throw an exception when no browser name was provided")
        void shouldThrowExceptionWhenNoBrowserNameWasProvided() {
            final MutableCapabilities desiredCapabilities = new MutableCapabilities();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> webDriverFactory.createWebDriver(desiredCapabilities));
        }

    }

    @Nested
    @DisplayName("when creating WebDriver instances without BinaryManager")
    class CreateWebDriverTestWithoutBinaryManager {

        @BeforeEach
        void beforeEach() {
            webDriverFactory = new LocalWebDriverFactory();
        }

        @Test
        @DisplayName("should throw exception when a browser requires a WebDriver binary")
        void shouldThrowExceptionWhenRequestingBrowserThatRequiresBinaryWhenBinaryManagerIsNull() {
            assertThatExceptionOfType(WebDriverFactoryException.class)
                    .isThrownBy(() -> webDriverFactory.createWebDriver(new ChromeOptions()))
                    .withMessageContaining(BinaryManager.class.getSimpleName());
        }

    }

}