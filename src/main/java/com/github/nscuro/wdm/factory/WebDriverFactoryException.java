package com.github.nscuro.wdm.factory;

import org.openqa.selenium.Capabilities;

import static java.lang.String.format;

/**
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
