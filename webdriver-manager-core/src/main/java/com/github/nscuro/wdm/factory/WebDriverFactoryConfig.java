package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.Browser;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class WebDriverFactoryConfig {

    private Map<Browser, String> binaryVersions;

    WebDriverFactoryConfig() {
        binaryVersions = new HashMap<>();
    }

    /**
     * Specify which driver binary version to download for a given {@link Browser}.
     *
     * @param browser The {@link Browser} this preference should apply for
     * @param version The desired version
     */
    public void setBinaryVersionForBrowser(final Browser browser, final String version) {
        binaryVersions.put(browser, version);
    }

    /**
     *
     * @param browser
     * @return
     */
    public Optional<String> getBinaryVersionForBrowser(final Browser browser) {
        return binaryVersions.entrySet().stream()
                .filter(entry -> entry.getKey() == browser)
                .map(Map.Entry::getValue)
                .findAny();
    }

}
