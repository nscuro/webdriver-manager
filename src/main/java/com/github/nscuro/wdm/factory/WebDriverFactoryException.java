package com.github.nscuro.wdm.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import static java.lang.String.format;

/**
 * Exception that is thrown when {@link WebDriver} instantiation via {@link WebDriverFactory} failed.
 *
 * @since 0.1.2
 */
public class WebDriverFactoryException extends RuntimeException {

    WebDriverFactoryException(final Capabilities capabilities, final String message) {
        super(format("Couldn't create WebDriver instance for %s: %s", capabilities, message));
    }

    private WebDriverFactoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    WebDriverFactoryException(final Capabilities capabilities, final Throwable cause) {
        this(format("Couldn't create WebDriver instance for %s", capabilities), cause);
    }

}
