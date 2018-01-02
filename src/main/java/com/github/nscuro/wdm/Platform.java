package com.github.nscuro.wdm;

import javax.annotation.Nonnull;
import java.util.Set;

public interface Platform {

    @Nonnull
    String getName();

    @Nonnull
    Os getOs();

    @Nonnull
    Set<Architecture> getArchitectures();

}
