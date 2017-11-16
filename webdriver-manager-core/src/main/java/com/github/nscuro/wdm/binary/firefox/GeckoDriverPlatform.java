package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

enum GeckoDriverPlatform {

    WIN32(Os.WINDOWS, singletonList(Architecture.X86)),

    WIN64(Os.WINDOWS, singletonList(Architecture.X64)),

    LINUX64(Os.LINUX, singletonList(Architecture.X64)),

    MACOS(Os.MACOS, singletonList(Architecture.X64));

    private final Os os;

    private final List<Architecture> architectures;

    GeckoDriverPlatform(final Os os, final List<Architecture> architectures) {
        this.os = os;
        this.architectures = architectures;
    }

    static GeckoDriverPlatform from(final Os os, final Architecture architecture) {
        return Arrays.stream(values())
                .filter(platform -> platform.os.equals(os))
                .filter(platform -> platform.architectures.contains(architecture))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

}
