package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.binary.BinaryManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public final class LocalWebDriverFactory implements WebDriverFactory {

    private final BinaryManager binaryManager;

    private final WebDriverFactoryConfig config;

    public LocalWebDriverFactory(final BinaryManager binaryManager,
                                 final WebDriverFactoryConfig config) {
        this.binaryManager = binaryManager;
        this.config = config;
    }

    public LocalWebDriverFactory(final BinaryManager binaryManager) {
        this(binaryManager, new WebDriverFactoryConfig());
    }

    /**
     * Get a {@link WebDriver} instance based on the given {@link Capabilities}.
     * <p>
     * Per default, {@link LocalWebDriverFactory} will always attempt to download
     * the latest version of a binary when necessary. This behavior can be altered
     * by providing a {@link WebDriverFactoryConfig} at construction which specifies
     * a specific version on a per-browser basis.
     * <p>
     * Note that {@link Capabilities#getPlatform()} will be ignored.
     * Instead, the current operating system and architecture will be detected
     * and a matching binary will be downloaded (if possible and / or necessary).
     *
     * @param capabilities The desired capabilities
     * @return A {@link WebDriver} instance
     * @throws WebDriverFactoryException When creating the {@link WebDriver} instance failed
     * @throws IllegalArgumentException  When the given {@link Capabilities} do not specify a browser name
     */
    @Nonnull
    @Override
    public WebDriver createWebDriver(final Capabilities capabilities) {
        final Browser browser = Optional.ofNullable(capabilities.getBrowserName())
                .filter(browserName -> !browserName.isEmpty())
                .map(Browser::byName)
                .orElseThrow(() -> new IllegalArgumentException("No browser name specified"));

        if (browser.doesRequireBinary()) {
            final File webDriverBinary;

            final Optional<String> desiredVersion = config.getBinaryVersionForBrowser(browser);

            try {
                if (desiredVersion.isPresent()) {
                    webDriverBinary = binaryManager.getBinary(browser, desiredVersion.get());
                } else {
                    webDriverBinary = binaryManager.getBinary(browser);
                }
            } catch (IOException e) {
                throw new WebDriverFactoryException(capabilities, e);
            }

            binaryManager.registerBinary(webDriverBinary, browser);
        }

        return getWebDriverInstance(browser, capabilities);
    }

    private WebDriver getWebDriverInstance(final Browser browser, final Capabilities capabilities) {
        try {
            return Class
                    .forName(browser.getWebDriverClassName())
                    .asSubclass(WebDriver.class)
                    .getConstructor(Capabilities.class)
                    .newInstance(capabilities);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException e) {
            throw new WebDriverFactoryException(capabilities, e);
        }
    }

}
