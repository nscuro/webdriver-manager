package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.binary.BinaryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.Optional;

class LocalWebDriverFactoryIT {

    private BinaryManager binaryManager;

    private WebDriverFactory webDriverFactory;

    private WebDriver webDriver;

    @BeforeEach
    void beforeEach() {
        binaryManager = BinaryManager.createDefault();

        webDriverFactory = new LocalWebDriverFactory(binaryManager);
    }

    @Test
    void testGetWebDriverChrome() throws IOException {
        webDriver = webDriverFactory.getWebDriver(new ChromeOptions());
    }

    @Test
    void testGetWebDriverFirefox() throws IOException {
        webDriver = webDriverFactory.getWebDriver(new FirefoxOptions());
    }

    @Test
    void testGetWebDriverHtmlUnit() throws IOException {
        webDriver = webDriverFactory.getWebDriver(DesiredCapabilities.htmlUnit());
    }

    @AfterEach
    void afterEach() {
        Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);

        binaryManager.cleanUp();
    }

}
