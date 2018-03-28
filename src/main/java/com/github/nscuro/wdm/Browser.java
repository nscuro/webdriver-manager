package com.github.nscuro.wdm;

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

    CHROME(Arrays.asList(BrowserType.CHROME, BrowserType.GOOGLECHROME), "org.openqa.selenium.chrome.ChromeDriver", "webdriver.chrome.driver", true),

    EDGE(Collections.singletonList(BrowserType.EDGE), "org.openqa.selenium.edge.EdgeDriver", "webdriver.edge.driver", true),

    FIREFOX(Collections.singletonList(BrowserType.FIREFOX), "org.openqa.selenium.firefox.FirefoxDriver", "webdriver.gecko.driver", true),

    INTERNET_EXPLORER(Arrays.asList(BrowserType.IE, BrowserType.IEXPLORE), "org.openqa.selenium.ie.InternetExplorerDriver", "webdriver.ie.driver", true),

    OPERA(Arrays.asList(BrowserType.OPERA, BrowserType.OPERA_BLINK), "org.openqa.selenium.opera.OperaDriver", "webdriver.opera.driver", true),

    /* PHANTOM_JS(Collections.singletonList(BrowserType.PHANTOMJS), "org.openqa.selenium.phantomjs.PhantomJSDriver", "phantomjs.binary.path", true), */

    HTMLUNIT(Collections.singletonList(BrowserType.HTMLUNIT), "org.openqa.selenium.htmlunit.HtmlUnitDriver", null, false),

    SAFARI(Collections.singletonList(BrowserType.SAFARI), "org.openqa.selenium.safari.SafariDriver", null, false);

    private final List<String> names;

    private final String webDriverClassName;

    private final String binarySystemProperty;

    private final boolean requiresBinary;

    Browser(final List<String> names,
            final String webDriverClassName,
            @Nullable final String binarySystemProperty,
            final boolean requiresBinary) {
        this.names = names;
        this.webDriverClassName = webDriverClassName;
        this.binarySystemProperty = binarySystemProperty;
        this.requiresBinary = requiresBinary;
    }

    private boolean hasName(final String name) {
        return names.contains(name);
    }

    @Nonnull
    List<String> getNames() {
        return names;
    }

    @Nonnull
    public String getWebDriverClassName() {
        return webDriverClassName;
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
