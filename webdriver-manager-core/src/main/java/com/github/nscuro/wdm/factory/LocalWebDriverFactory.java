package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.Browser;
import com.github.nscuro.wdm.binary.BinaryManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class LocalWebDriverFactory implements WebDriverFactory {

    private final BinaryManager binaryManager;

    public LocalWebDriverFactory(final BinaryManager binaryManager) {
        this.binaryManager = binaryManager;
    }

    /**
     * Get a {@link WebDriver} instance based on the given {@link Capabilities}.
     * <p>
     * Note that {@link Capabilities#getPlatform()} will be ignored.
     * <p>
     * Instead, the current operating system and architecture will be detected
     * and a matching binary will be downloaded (if possible and / or necessary).
     *
     * @param capabilities The desired capabilities
     * @return A {@link WebDriver} instance
     * @throws IOException              When downloading the binary failed
     * @throws IllegalArgumentException When the given {@link Capabilities} do not specify a browser name
     */
    @Override
    public WebDriver getWebDriver(final Capabilities capabilities) throws IOException {
        final Browser browser = Optional.ofNullable(capabilities.getBrowserName())
                .map(Browser::byName)
                .orElseThrow(() -> new IllegalArgumentException("No browser name specified"));

        final File webDriverBinary = binaryManager.getBinary(browser);

        binaryManager.registerBinary(webDriverBinary, browser);

        return getWebDriverInstance(browser, capabilities);
    }

    private WebDriver getWebDriverInstance(final Browser browser, final Capabilities capabilities) {
        try {
            return browser.getWebDriverClass()
                    .getConstructor(Capabilities.class)
                    .newInstance(capabilities);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // TODO Don't be so sloppy with these exceptions
            throw new RuntimeException(e);
        }
    }

}
