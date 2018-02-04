package com.github.nscuro.wdm.manager;

import com.github.nscuro.wdm.factory.WebDriverFactory;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A {@link WebDriverManager} that manages a single {@link WebDriver} instance.
 *
 * @since 0.1.3
 */
public class SingletonWebDriverManager implements WebDriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonWebDriverManager.class);

    private final WebDriverFactory webDriverFactory;

    private WebDriver currentWebDriver;

    private Capabilities currentCapabilities;

    public SingletonWebDriverManager(final WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    /**
     * Get a {@link WebDriver} instance for the given desired {@link Capabilities}.
     * <pre>
     *  -   If no {@link WebDriver} is currently active, a new instance will be created.
     *  -   If a {@link WebDriver} is currently active and its {@link Capabilities} match the given desired ones,
     *      the currently active instance will be returned.
     *  -   If a {@link WebDriver} is currently active and its {@link Capabilities} DO NOT match the given
     *      desired ones, the currently active instance will be closed and a new one will be created.
     * </pre>
     *
     * @param capabilities The desired {@link Capabilities}
     * @return A {@link WebDriver} instance
     */
    @Nonnull
    @Override
    public synchronized WebDriver getWebDriver(final Capabilities capabilities) {
        if (currentWebDriver == null) {
            currentWebDriver = webDriverFactory.createWebDriver(capabilities);
            currentCapabilities = capabilities;
        } else {
            if (!Objects.equals(currentCapabilities, capabilities)) {
                LOGGER.debug("New desired capabilities detected; Quitting current WebDriver instance...");

                quitWebDriver(currentWebDriver);

                return getWebDriver(capabilities);
            }
        }

        return currentWebDriver;
    }

    /**
     * Quit the given {@link WebDriver} instance.
     *
     * @param webDriver The {@link WebDriver} instance to quit
     * @throws IllegalArgumentException When the given instance is not managed by this class
     */
    @Override
    public synchronized void quitWebDriver(final WebDriver webDriver) {
        if (currentWebDriver == null) {
            throw new IllegalStateException("Cannot quit any WebDriver instance: No instance is currently active");
        } else if (currentWebDriver != requireNonNull(webDriver, "No WebDriver instance provided")) {
            throw new IllegalArgumentException("The given WebDriver instance is not managed by this class");
        }

        webDriver.quit();

        currentWebDriver = null;
        currentCapabilities = null;
    }

    /**
     * @see #quitWebDriver(WebDriver)
     */
    @Override
    public void shutdown() {
        Optional.ofNullable(currentWebDriver).ifPresent(this::quitWebDriver);
    }

    public Optional<WebDriver> getCurrentWebDriver() {
        return Optional.ofNullable(currentWebDriver);
    }

    public Optional<Capabilities> getCurrentCapabilities() {
        return Optional.ofNullable(currentCapabilities);
    }

    void setCurrentWebDriver(final WebDriver webDriver) {
        this.currentWebDriver = webDriver;
    }

}
