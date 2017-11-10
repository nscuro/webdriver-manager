package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;

public enum ChromeDriverPlatform {

    WIN32,

    MAC64,

    LINUX32,

    LINUX64;

    static ChromeDriverPlatform from(final Os os, final Architecture architecture) {
        switch (os) {
            case WINDOWS:
                return WIN32;
            case MACOS:
                if (architecture.equals(Architecture.X86)) {
                    throw new IllegalArgumentException();
                } else {
                    return MAC64;
                }
            case LINUX:
                switch (architecture) {
                    case X64:
                        return LINUX64;
                    case X86:
                        return LINUX32;
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }

}
