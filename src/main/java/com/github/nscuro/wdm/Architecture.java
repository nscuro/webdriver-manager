package com.github.nscuro.wdm;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public enum Architecture {

    X86(Arrays.asList("x86", "i386", "i686", "i486", "i86")),

    X64(Arrays.asList("amd64", "ia64", "x86_64"));

    private final List<String> names;

    Architecture(final List<String> names) {
        this.names = names;
    }

    List<String> getNames() {
        return unmodifiableList(names);
    }

    private boolean hasName(final String name) {
        return names.contains(name);
    }

    @Nonnull
    public static Architecture getCurrent() {
        final String archName = System.getProperty("os.arch").toLowerCase();

        return Arrays.stream(values())
                .filter(architecture -> architecture.hasName(archName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to determine the current architecture"));
    }

}
