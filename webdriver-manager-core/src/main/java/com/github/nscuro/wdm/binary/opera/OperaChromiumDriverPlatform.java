package com.github.nscuro.wdm.binary.opera;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import com.github.nscuro.wdm.Platform;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Platforms that are supported by OperaChromiumDriver
 */
enum OperaChromiumDriverPlatform implements Platform {

    LINUX64(Os.LINUX, Architecture.X64),

    /**
     * @deprecated Since 2.29, 32bit Linux is not supported anmore,
     *         see <a href="https://github.com/operasoftware/operachromiumdriver/releases/tag/v.2.29">release notes</a>
     */
    LINUX32(Os.LINUX, Architecture.X86),

    MAC64(Os.MACOS, Architecture.X64),

    WIN32(Os.WINDOWS, Architecture.X86),

    WIN64(Os.WINDOWS, Architecture.X64);

    private final Os os;

    private final Architecture architecture;

    OperaChromiumDriverPlatform(final Os os, final Architecture architecture) {
        this.os = os;
        this.architecture = architecture;
    }

    static Platform valueOf(final Os os, final Architecture architecture) {
        return Arrays.stream(values())
                .filter(platform -> platform.getOs() == os)
                .filter(platform -> platform.getArchitectures().contains(architecture))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
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
        return Collections.singleton(architecture);
    }

}
