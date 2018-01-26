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
 */
public class SingleWebDriverManager implements WebDriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleWebDriverManager.class);

    private final WebDriverFactory webDriverFactory;

    private WebDriver currentWebDriver;

    private Capabilities currentCapabilities;

    public SingleWebDriverManager(final WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    @Nonnull
    @Override
    public WebDriver getWebDriver(final Capabilities capabilities) {
        if (currentWebDriver == null) {
            currentWebDriver = webDriverFactory.createWebDriver(capabilities);
            currentCapabilities = capabilities;
        } else {
            if (!Objects.equals(currentCapabilities, capabilities)) {
                LOGGER.trace("New desired capabilities detected; quitting current WebDriver instance...");

                quitWebDriver(currentWebDriver);

                return getWebDriver(capabilities);
            }
        }

        return currentWebDriver;
    }

    @Override
    public void quitWebDriver(final WebDriver webDriver) {
        requireNonNull(webDriver, "No WebDriver instance provided");

        if (currentWebDriver != webDriver) {
            throw new IllegalArgumentException("Cannot quit WebDriver instances that are not owned by this class");
        }

        webDriver.quit();

        currentWebDriver = null;
        currentCapabilities = null;
    }

    @Override
    public void shutdown() {
        Optional.ofNullable(currentWebDriver).ifPresent(this::quitWebDriver);
    }

    Optional<WebDriver> getCurrentWebDriver() {
        return Optional.ofNullable(currentWebDriver);
    }

    Optional<Capabilities> getCurrentCapabilities() {
        return Optional.ofNullable(currentCapabilities);
    }

}
