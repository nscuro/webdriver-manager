package com.github.nscuro.wdm.binary.chrome;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

enum ChromeDriverPlatform {

    WIN32(Os.WINDOWS, Arrays.asList(Architecture.X64, Architecture.X86)),

    MAC64(Os.MACOS, Collections.singletonList(Architecture.X64)),

    LINUX32(Os.LINUX, Collections.singletonList(Architecture.X86)),

    LINUX64(Os.LINUX, Collections.singletonList(Architecture.X64));

    private final Os os;

    private final List<Architecture> architectures;

    ChromeDriverPlatform(final Os os, final List<Architecture> architectures) {
        this.os = os;
        this.architectures = architectures;
    }

    static ChromeDriverPlatform from(final Os os, final Architecture architecture) {
        return Arrays.stream(values())
                .filter(platform -> platform.os.equals(os))
                .filter(platform -> platform.architectures.contains(architecture))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

}
