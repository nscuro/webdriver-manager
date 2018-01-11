package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariOptions;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
    void testGetWebDriverOpera() throws IOException {
        webDriver = webDriverFactory.getWebDriver(new OperaOptions());
    }

    @Test
    void testGetWebDriverHtmlUnit() throws IOException {
        webDriver = webDriverFactory.getWebDriver(DesiredCapabilities.htmlUnit());
    }

    @Test
    void testGetWebDriverSafari() throws IOException {
        assumeTrue(Os.getCurrent() == Os.MACOS, "SafariDriver is only available on MacOS");

        webDriver = webDriverFactory.getWebDriver(new SafariOptions());
    }

    @Test
    void testGetWebDriverEdge() throws IOException {
        assumeTrue(Os.getCurrent() == Os.WINDOWS, "EdgeDriver is only available on Windows");

        webDriver = webDriverFactory.getWebDriver(new EdgeOptions());
    }

    @AfterEach
    void afterEach() {
        Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);

        binaryManager.cleanUp();
    }

}
