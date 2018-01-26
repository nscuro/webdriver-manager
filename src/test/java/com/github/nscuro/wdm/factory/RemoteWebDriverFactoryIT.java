package com.github.nscuro.wdm.factory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Optional;

/**
 * In order for this integration test to work, a local Selenium Grid Hub instance is required.
 * <p>
 * An easy way to accomplish this is by using docker:
 * <pre>{@code
 *     1. docker run -d -P --name selenium-hub -e GRID_TIMEOUT=10 selenium/hub
 *     2. docker run -d --link selenium-hub:hub selenium/node-chrome
 * }</pre>
 * <p>
 * The Grid Hub should now be reachable at <em>http://localhost:32768/wd/hub</em>,
 * but you should check the port using {@code docker ps}.
 *
 * @see <a href="https://hub.docker.com/r/selenium/hub/">Selenium Grid Hub Docker documentation</a>
 */
class RemoteWebDriverFactoryIT {

    private WebDriverFactory webDriverFactory;

    private WebDriver webDriver;

    @BeforeEach
    void beforeEach() {
        webDriverFactory = new RemoteWebDriverFactory("http://localhost:32768/wd/hub");
    }

    @Test
    @Disabled("Requires a local Selenium Grid Hub instance")
    void shouldBeAbleToInitiateRemoteWebDriverInstances() {
        webDriver = webDriverFactory.createWebDriver(new ChromeOptions());
    }

    @AfterEach
    void afterEach() {
        Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
    }

}