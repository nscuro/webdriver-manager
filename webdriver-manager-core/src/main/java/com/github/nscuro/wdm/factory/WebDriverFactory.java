package com.github.nscuro.wdm.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

public interface WebDriverFactory {

    /**
     * Get a {@link WebDriver} instance based on the provided {@link Capabilities}.
     *
     * @param capabilities The desired capabilities
     * @return A {@link WebDriver} instance
     * @throws IOException              When downloading the binary failed
     * @throws IllegalArgumentException When the given {@link Capabilities} do not specify a browser name
     */
    WebDriver getWebDriver(final Capabilities capabilities) throws IOException;

}
