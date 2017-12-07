package com.github.nscuro.wdm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.BrowserType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;

public enum Browser {

    CHROME(Arrays.asList(BrowserType.CHROME, BrowserType.GOOGLECHROME), ChromeDriver.class, "webdriver.chrome.driver", true),

    EDGE(Collections.singletonList(BrowserType.EDGE), EdgeDriver.class, "webdriver.edge.driver", true),

    FIREFOX(Collections.singletonList(BrowserType.FIREFOX), FirefoxDriver.class, "webdriver.gecko.driver", true),

    INTERNET_EXPLORER(Arrays.asList(BrowserType.IE, BrowserType.IEXPLORE), InternetExplorerDriver.class, "webdriver.ie.driver", true),

    OPERA(Arrays.asList(BrowserType.OPERA, BrowserType.OPERA_BLINK), OperaDriver.class, "webdriver.opera.driver", true),

    PHANTOM_JS(Collections.singletonList(BrowserType.PHANTOMJS), PhantomJSDriver.class, "phantomjs.binary.path", true),

    HTMLUNIT(Collections.singletonList(BrowserType.HTMLUNIT), HtmlUnitDriver.class, null, false);

    private final List<String> names;

    private final Class<? extends WebDriver> webDriverClass;

    private final String binarySystemProperty;

    private final boolean requiresBinary;

    Browser(final List<String> names,
            final Class<? extends WebDriver> webDriverClass,
            @Nullable final String binarySystemProperty,
            final boolean requiresBinary) {
        this.names = names;
        this.webDriverClass = webDriverClass;
        this.binarySystemProperty = binarySystemProperty;
        this.requiresBinary = requiresBinary;
    }

    private boolean hasName(final String name) {
        return names.contains(name);
    }

    @Nonnull
    public Class<? extends WebDriver> getWebDriverClass() {
        return webDriverClass;
    }

    @Nonnull
    public Optional<String> getBinarySystemProperty() {
        return Optional.ofNullable(binarySystemProperty);
    }

    public boolean doesRequireBinary() {
        return requiresBinary;
    }

    @Nonnull
    public static Browser byName(final String name) {
        return Arrays.stream(values())
                .filter(browser -> browser.hasName(name))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(format("No browser named \"%s\" found", name)));
    }

}
