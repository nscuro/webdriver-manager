package com.github.nscuro.wdm.factory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.grid.internal.utils.configuration.StandaloneConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.server.SeleniumServer;

import java.util.Optional;

import static java.lang.String.format;

@DisplayName("The remote WebDriver factory")
class RemoteWebDriverFactoryIT {

    private static SeleniumServer seleniumServer;

    private WebDriverFactory webDriverFactory;

    private WebDriver webDriver;

    @BeforeAll
    static void beforeAll() {
        seleniumServer = new SeleniumServer(new StandaloneConfiguration());
        seleniumServer.boot();
    }

    @BeforeEach
    void beforeEach() {
        webDriverFactory = new RemoteWebDriverFactory(format("http://localhost:%d/wd/hub", seleniumServer.getRealPort()));
    }

    @Test
    @DisplayName("should be able to create remote WebDriver instances")
    void shouldBeAbleToCreateRemoteWebDriverInstances() {
        webDriver = webDriverFactory.createWebDriver(DesiredCapabilities.htmlUnit());
    }

    @AfterEach
    void afterEach() {
        Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
    }

    @AfterAll
    static void afterAll() {
        seleniumServer.stop();
    }

}