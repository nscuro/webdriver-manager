package com.github.nscuro.wdm.factory;

import org.openqa.selenium.Capabilities;

import static java.lang.String.format;

public class WebDriverFactoryException extends RuntimeException {

    private WebDriverFactoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    WebDriverFactoryException(final Capabilities capabilities, final Throwable cause) {
        this(format("Couldn't create WebDriver instance for %s", capabilities), cause);
    }

}
