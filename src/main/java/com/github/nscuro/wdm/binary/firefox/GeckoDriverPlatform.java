package com.github.nscuro.wdm.binary.firefox;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;

enum GeckoDriverPlatform implements Platform {

    WIN32(Os.WINDOWS, singletonList(Architecture.X86)),

    WIN64(Os.WINDOWS, singletonList(Architecture.X64)),

    LINUX32(Os.LINUX, singletonList(Architecture.X86)),

    LINUX64(Os.LINUX, singletonList(Architecture.X64)),

    MACOS(Os.MACOS, singletonList(Architecture.X64));

    private final Os os;

    private final List<Architecture> architectures;

    GeckoDriverPlatform(final Os os, final List<Architecture> architectures) {
        this.os = os;
        this.architectures = architectures;
    }

    static Optional<GeckoDriverPlatform> valueOf(final Os os, final Architecture architecture) {
        return Arrays.stream(values())
                .filter(platform -> platform.os.equals(os))
                .filter(platform -> platform.architectures.contains(architecture))
                .findAny();
    }

    @Nonnull
    @Override
    public String getName() {
        return name().toLowerCase();
    }

    @Nonnull
    @Override
    public Os getOs() {
        return os;
    }

    @Nonnull
    @Override
    public Set<Architecture> getArchitectures() {
        return new HashSet<>(architectures);
    }

}
