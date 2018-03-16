package com.github.nscuro.wdm.binary.util.googlecs;

/**
 * @since 0.2.0
 */
public class GoogleCloudStorageEntry {

    private final String key;

    private final String url;

    public GoogleCloudStorageEntry(final String key, final String url) {
        this.key = key;
        this.url = url;
    }

    public final String getKey() {
        return key;
    }

    public final String getUrl() {
        return url;
    }

}
