package com.github.nscuro.wdm.manager;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface WebDriverManager {

    @Nonnull
    WebDriver getWebDriver(final Capabilities capabilities) throws IOException;

    void quitWebDriver(final WebDriver webDriver);

    void shutdown();

}
