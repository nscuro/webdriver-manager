package com.github.nscuro.wdm;

import javax.annotation.Nonnull;

public enum Os {

    WINDOWS,

    LINUX,

    MACOS;

    @Nonnull
    public static Os getCurrent() {
        final String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            return WINDOWS;
        } else if (osName.contains("linux")) {
            return LINUX;
        } else if (osName.contains("mac")) {
            return MACOS;
        } else {
            throw new IllegalStateException("Unable to determine the current operating system");
        }
    }

}
