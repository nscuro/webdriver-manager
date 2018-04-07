package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.binary.BinaryManager;
import com.github.nscuro.wdm.binary.chrome.ChromeDriverBinaryProvider;
import com.github.nscuro.wdm.binary.edge.MicrosoftWebDriverBinaryProvider;
import com.github.nscuro.wdm.binary.firefox.GeckoDriverBinaryProvider;
import com.github.nscuro.wdm.binary.ie.IEDriverServerBinaryProvider;
import com.github.nscuro.wdm.binary.opera.OperaChromiumDriverBinaryProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The local WebDriver factory")
class LocalWebDriverFactoryIT {

    private static BinaryManager binaryManager;

    private static WebDriverFactory webDriverFactory;

    private WebDriver webDriver;

    @BeforeAll
    static void beforeAll() throws IOException {
        binaryManager = BinaryManager
                .builder()
                .defaultHttpClient()
                .binaryDestinationDir(Files.createTempDirectory(LocalWebDriverFactoryIT.class.getSimpleName()))
                .addBinaryProvider(ChromeDriverBinaryProvider::new)
                .addBinaryProvider(MicrosoftWebDriverBinaryProvider::new)
                .addBinaryProvider(GeckoDriverBinaryProvider::new)
                .addBinaryProvider(IEDriverServerBinaryProvider::new)
                .addBinaryProvider(OperaChromiumDriverBinaryProvider::new)
                .build();

        webDriverFactory = new LocalWebDriverFactory(binaryManager);
    }

    @Test
    @DisplayName("should be able to create ChromeDriver instances")
    void testCreateWebDriverChrome() {
        webDriver = webDriverFactory.createWebDriver(new ChromeOptions());

        assertThat(webDriver).isInstanceOf(ChromeDriver.class);
    }

    @Test
    @DisplayName("should be able to create FirefoxDriver instances")
    void testCreateWebDriverFirefox() {
        webDriver = webDriverFactory.createWebDriver(new FirefoxOptions());

        assertThat(webDriver).isInstanceOf(FirefoxDriver.class);
    }

    @Test
    @DisplayName("should be able to create OperaDriver instances")
    @Disabled("Opera is not installed on most environments. Re-enable for local testing if desired.")
    void testCreateWebDriverOpera() {
        webDriver = webDriverFactory.createWebDriver(new OperaOptions());

        assertThat(webDriver).isInstanceOf(OperaDriver.class);
    }

    @Test
    @DisplayName("should be able to create HtmlUnit instances")
    void testCreateWebDriverHtmlUnit() {
        webDriver = webDriverFactory.createWebDriver(DesiredCapabilities.htmlUnit());

        assertThat(webDriver).isInstanceOf(HtmlUnitDriver.class);
    }

    @Test
    @EnabledOnOs(OS.MAC)
    @DisplayName("should be able to create SafariDriver instances")
    void testCreateWebDriverSafari() {
        webDriver = webDriverFactory.createWebDriver(new SafariOptions());

        assertThat(webDriver).isInstanceOf(SafariDriver.class);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("should be able to create EdgeDriver instances")
    void testCreateWebDriverEdge() {
        webDriver = webDriverFactory.createWebDriver(new EdgeOptions());

        assertThat(webDriver).isInstanceOf(EdgeDriver.class);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("should be able to create InternetExplorerDriver instances")
    void testCreateWebDriverInternetExplorer() {
        webDriver = webDriverFactory.createWebDriver(new InternetExplorerOptions());

        assertThat(webDriver).isInstanceOf(InternetExplorerDriver.class);
    }

    @AfterEach
    void afterEach() {
        Optional
                .ofNullable(webDriver)
                .ifPresent(WebDriver::quit);

        //noinspection ResultOfMethodCallIgnored
        binaryManager
                .getLocalWebDriverBinaries()
                .forEach(File::delete);
    }

}
