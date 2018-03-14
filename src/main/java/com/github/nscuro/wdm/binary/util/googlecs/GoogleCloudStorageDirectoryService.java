package com.github.nscuro.wdm.binary.util.googlecs;

import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @since 0.2.0
 */
public interface GoogleCloudStorageDirectoryService {

    @Nonnull
    List<GoogleCloudStorageEntry> getEntries() throws IOException;

    @Nonnull
    File downloadFile(final GoogleCloudStorageEntry fileEntry) throws IOException;

    @Nonnull
    static GoogleCloudStorageDirectoryService create(final HttpClient httpClient,
                                                     final String directoryUrl) {
        return new GoogleCloudStorageDirectoryServiceImpl(httpClient, directoryUrl);
    }

}
