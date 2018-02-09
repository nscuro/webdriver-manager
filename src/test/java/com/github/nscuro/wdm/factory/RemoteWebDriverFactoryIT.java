package com.github.nscuro.wdm.factory;

import org.junit.jupiter.api.*;
import org.openqa.grid.internal.utils.configuration.StandaloneConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.server.SeleniumServer;

import java.util.Optional;

import static java.lang.String.format;

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
    void shouldBeAbleToInitiateRemoteWebDriverInstances() {
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