package com.github.nscuro.wdm.manager;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;

public interface WebDriverManager {

    /**
     * Get a {@link WebDriver} instance based on the given desired {@link Capabilities}.
     *
     * @param capabilities The desired {@link Capabilities}
     * @return A {@link WebDriver} instance
     */
    @Nonnull
    WebDriver getWebDriver(final Capabilities capabilities);

    /**
     * Quit a given {@link WebDriver} instance.
     *
     * @param webDriver The {@link WebDriver} instance to quit
     */
    void quitWebDriver(final WebDriver webDriver);

    /**
     * Quit all open {@link WebDriver} instances managed by this class.
     */
    void shutdown();

}
