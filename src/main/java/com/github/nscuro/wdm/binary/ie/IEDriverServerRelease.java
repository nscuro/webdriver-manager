package com.github.nscuro.wdm.binary.ie;

import com.github.nscuro.wdm.Architecture;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class IEDriverServerRelease {

    private final String version;

    private final Architecture architecture;

    private final String downloadUrl;

}
