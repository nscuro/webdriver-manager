package com.github.nscuro.wdm.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * A {@link WebDriverFactory} that creates {@link WebDriver} instances remotely.
 */
public final class RemoteWebDriverFactory implements WebDriverFactory {

    private final URL gridHubUrl;

    /**
     * @param gridHubUrl URL of the Selenium grid hub
     * @throws IllegalArgumentException When the given URL is invalid or no
     *                                  URL was provided at all
     */
    public RemoteWebDriverFactory(final String gridHubUrl) {
        this.gridHubUrl = requireValidUrl(gridHubUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public WebDriver createWebDriver(final Capabilities capabilities) {
        return new RemoteWebDriver(gridHubUrl, capabilities);
    }

    @Nonnull
    static URL requireValidUrl(@Nullable final String url) {
        return Optional.ofNullable(url)
                .map(urlStr -> {
                    try {
                        return new URL(urlStr);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("No Grid Hub URL provided"));
    }

}
