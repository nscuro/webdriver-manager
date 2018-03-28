package com.github.nscuro.wdm.binary.ie;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.binary.util.googlecs.GoogleCloudStorageEntry;

final class IEDriverServerRelease extends GoogleCloudStorageEntry {

    private final String version;

    private final Architecture architecture;

    IEDriverServerRelease(final String key, final String url,
                          final String version, final Architecture architecture) {
        super(key, url);

        this.version = version;
        this.architecture = architecture;
    }

    public String getVersion() {
        return version;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

}
