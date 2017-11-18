package com.github.nscuro.wdm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.BrowserType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public enum Browser {

    CHROME(Arrays.asList(BrowserType.CHROME, BrowserType.GOOGLECHROME), ChromeDriver.class, "webdriver.chrome.driver"),

    EDGE(Collections.singletonList(BrowserType.EDGE), EdgeDriver.class, "webdriver.edge.driver"),

    FIREFOX(Collections.singletonList(BrowserType.FIREFOX), FirefoxDriver.class, "webdriver.gecko.driver"),

    INTERNET_EXPLORER(Arrays.asList(BrowserType.IE, BrowserType.IEXPLORE), InternetExplorerDriver.class, "webdriver.ie.driver"),

    OPERA(Collections.singletonList(BrowserType.OPERA), OperaDriver.class, "webdriver.opera.driver"),

    PHANTOM_JS(Collections.singletonList(BrowserType.PHANTOMJS), PhantomJSDriver.class, "phantomjs.binary.path");

    private final List<String> names;

    private final Class<? extends WebDriver> webDriverClass;

    private final String binarySystemProperty;

    Browser(final List<String> names,
            final Class<? extends WebDriver> webDriverClass,
            final String binarySystemProperty) {
        this.names = names;
        this.webDriverClass = webDriverClass;
        this.binarySystemProperty = binarySystemProperty;
    }

    private boolean hasName(final String name) {
        return names.contains(name);
    }

    @Nonnull
    public Class<? extends WebDriver> getWebDriverClass() {
        return webDriverClass;
    }

    @Nonnull
    public String getBinarySystemProperty() {
        return binarySystemProperty;
    }

    @Nonnull
    public static Browser byName(final String name) {
        return Arrays.stream(values())
                .filter(browser -> browser.hasName(name))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

}
