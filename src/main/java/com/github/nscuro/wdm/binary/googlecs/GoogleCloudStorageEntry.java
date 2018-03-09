package com.github.nscuro.wdm.binary.googlecs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleCloudStorageEntry {

    private final String key;

    private final String url;

    private final Long size;

}
