package com.github.nscuro.wdm;

public enum Browser {

    CHROME("webdriver.chrome.driver"),

    EDGE("webdriver.edge.driver"),

    FIREFOX("webdriver.gecko.driver"),

    INTERNET_EXPLORER("webdriver.ie.driver"),

    OPERA("webdriver.opera.driver"),

    PHANTOM_JS("phantomjs.binary.path");

    private final String binarySystemProperty;

    Browser(final String binarySystemProperty) {
        this.binarySystemProperty = binarySystemProperty;
    }

    public String getBinarySystemProperty() {
        return binarySystemProperty;
    }

}
