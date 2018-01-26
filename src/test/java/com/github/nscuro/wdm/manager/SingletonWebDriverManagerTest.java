package com.github.nscuro.wdm.manager;

import com.github.nscuro.wdm.factory.WebDriverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SingletonWebDriverManagerTest {

    private WebDriverFactory webDriverFactory;

    private SingletonWebDriverManager webDriverManager;

    @BeforeEach
    void beforeEach() {
        webDriverFactory = mock(WebDriverFactory.class);

        webDriverManager = new SingletonWebDriverManager(webDriverFactory);
    }

    @Nested
    class GetWebDriverTest {

        @Test
        void shouldReturnNewWebDriverInstanceWhenNoCurrentInstanceExists() {
            final WebDriver webDriverInstance = mock(WebDriver.class);

            final Capabilities capabilities = new ChromeOptions();

            given(webDriverFactory.createWebDriver(any(Capabilities.class)))
                    .willReturn(webDriverInstance);

            assertThat(webDriverManager.getWebDriver(capabilities)).isEqualTo(webDriverInstance);
            assertThat(webDriverManager.getCurrentWebDriver()).contains(webDriverInstance);
            assertThat(webDriverManager.getCurrentCapabilities()).contains(capabilities);
        }

        @Test
        void shouldReturnCurrentWebDriverInstanceWhenCapabilitiesAreTheSame() {
            final WebDriver webDriverInstance = mock(WebDriver.class);

            given(webDriverFactory.createWebDriver(any(Capabilities.class)))
                    .willReturn(webDriverInstance)
                    .willThrow(new AssertionError("webDriverFactory should only be called once"));

            assertThat(webDriverManager.getWebDriver(new ChromeOptions())).isEqualTo(webDriverInstance);
            assertThat(webDriverManager.getWebDriver(new ChromeOptions())).isEqualTo(webDriverInstance);
        }

        @Test
        void shouldQuitCurrentInstanceAndReturnNewOneWhenCapabilitiesDiffer() {
            final WebDriver initialWebDriverInstance = mock(WebDriver.class);

            final Capabilities initialCapabilities = new ChromeOptions();

            given(webDriverFactory.createWebDriver(eq(initialCapabilities)))
                    .willReturn(initialWebDriverInstance);

            final WebDriver secondWebDriverInstance = mock(WebDriver.class);

            final Capabilities secondCapabilities = new FirefoxOptions();

            given(webDriverFactory.createWebDriver(eq(secondCapabilities)))
                    .willReturn(secondWebDriverInstance);


            assertThat(webDriverManager.getWebDriver(initialCapabilities)).isEqualTo(initialWebDriverInstance);

            assertThat(webDriverManager.getWebDriver(secondCapabilities)).isEqualTo(secondWebDriverInstance);
            assertThat(webDriverManager.getCurrentWebDriver()).contains(secondWebDriverInstance);
            assertThat(webDriverManager.getCurrentCapabilities()).contains(secondCapabilities);
        }

    }

    @Nested
    class QuitWebDriverTest {

        @Test
        void shouldQuitGivenWebDriverWhenItsCurrentlyActive() {
            final WebDriver webDriver = mock(WebDriver.class);

            webDriverManager.setCurrentWebDriver(webDriver);

            webDriverManager.quitWebDriver(webDriver);

            verify(webDriver).quit();

            assertThat(webDriverManager.getCurrentWebDriver()).isNotPresent();
            assertThat(webDriverManager.getCurrentCapabilities()).isNotPresent();
        }

        @Test
        void shouldThrowExceptioWhenGivenWebDriverIsNotTheCurrentlyActiveOne() {
            final WebDriver currentlyActiveWebDriver = mock(WebDriver.class);

            webDriverManager.setCurrentWebDriver(currentlyActiveWebDriver);

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> webDriverManager.quitWebDriver(mock(WebDriver.class)));
        }

        @Test
        void shouldThrowExceptionWhenNoWebDriverIsCurrentlyActive() {
            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> webDriverManager.quitWebDriver(mock(WebDriver.class)));
        }

    }

}