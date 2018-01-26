package com.github.nscuro.wdm.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;

public interface WebDriverFactory {

    /**
     * Get a {@link WebDriver} instance based on the provided {@link Capabilities}.
     *
     * @param capabilities The desired capabilities
     * @return A {@link WebDriver} instance
     */
    @Nonnull
    WebDriver createWebDriver(final Capabilities capabilities);

}
