package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.binary.BinaryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariOptions;

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
    void testCreateWebDriverChrome() {
        webDriver = webDriverFactory.createWebDriver(new ChromeOptions());
    }

    @Test
    void testCreateWebDriverFirefox() {
        webDriver = webDriverFactory.createWebDriver(new FirefoxOptions());
    }

    @Test
    @Disabled("Opera is not installed on most environments. Re-enable for local testing if desired.")
    void testCreateWebDriverOpera() {
        webDriver = webDriverFactory.createWebDriver(new OperaOptions());
    }

    @Test
    void testCreateWebDriverHtmlUnit() {
        webDriver = webDriverFactory.createWebDriver(DesiredCapabilities.htmlUnit());
    }

    @Test
    void testCreateWebDriverSafari() {
        assumeTrue(Os.getCurrent() == Os.MACOS, "SafariDriver is only available on MacOS");

        webDriver = webDriverFactory.createWebDriver(new SafariOptions());
    }

    @Test
    void testCreateWebDriverEdge() {
        assumeTrue(Os.getCurrent() == Os.WINDOWS, "EdgeDriver is only available on Windows");

        webDriver = webDriverFactory.createWebDriver(new EdgeOptions());
    }

    @Test
    void testCreateWebDriverInternetExplorer() {
        assumeTrue(Os.getCurrent() == Os.WINDOWS, "InternetExplorer is only available on Windows");

        webDriver = webDriverFactory.createWebDriver(new InternetExplorerOptions());
    }

    @AfterEach
    void afterEach() {
        Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
    }

}
