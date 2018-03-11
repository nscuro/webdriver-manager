package com.github.nscuro.wdm.binary.chrome;

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

enum ChromeDriverPlatform implements Platform {

    WIN32(Os.WINDOWS, Arrays.asList(Architecture.X64, Architecture.X86)),

    MAC64(Os.MACOS, singletonList(Architecture.X64)),

    LINUX32(Os.LINUX, singletonList(Architecture.X86)),

    LINUX64(Os.LINUX, singletonList(Architecture.X64));

    private final Os os;

    private final List<Architecture> architectures;

    ChromeDriverPlatform(final Os os, final List<Architecture> architectures) {
        this.os = os;
        this.architectures = architectures;
    }

    @Deprecated // TODO: Delete once ChromeDriverBinaryDownloader has been completely replaced
    static ChromeDriverPlatform valueOf(final Os os, final Architecture architecture) {
        return Arrays.stream(values())
                .filter(platform -> platform.os.equals(os))
                .filter(platform -> platform.architectures.contains(architecture))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    static Optional<ChromeDriverPlatform> valueOf2(final Os os, final Architecture architecture) {
        return Arrays.stream(values())
                .filter(platform -> platform.os == os)
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
