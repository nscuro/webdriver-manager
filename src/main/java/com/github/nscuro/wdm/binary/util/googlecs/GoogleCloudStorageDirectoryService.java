package com.github.nscuro.wdm.binary.util.googlecs;

import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A service for accessing Google's Cloud Storage API.
 *
 * @see <a href="https://cloud.google.com/storage/docs/xml-api/overview">API documentation</a>
 * @since 0.2.0
 */
public interface GoogleCloudStorageDirectoryService {

    /**
     * Get all {@link GoogleCloudStorageEntry} of the directory.
     *
     * @return All {@link GoogleCloudStorageEntry} of the directory
     * @throws IOException In case of a networking error
     */
    @Nonnull
    List<GoogleCloudStorageEntry> getEntries() throws IOException;

    /**
     * Download a given {@link GoogleCloudStorageEntry}.
     *
     * @param fileEntry The {@link GoogleCloudStorageEntry} to download
     * @return The downloaded {@link File}
     * @throws IOException In case of a networking error
     */
    @Nonnull
    File downloadFile(final GoogleCloudStorageEntry fileEntry) throws IOException;

    /**
     * Create a new {@link GoogleCloudStorageDirectoryService} instance.
     *
     * @param httpClient   The {@link HttpClient} to use
     * @param directoryUrl The directory URL to use
     * @return A new {@link GoogleCloudStorageDirectoryService} instance
     */
    @Nonnull
    static GoogleCloudStorageDirectoryService create(final HttpClient httpClient,
                                                     final String directoryUrl) {
        return new GoogleCloudStorageDirectoryServiceImpl(httpClient, directoryUrl);
    }

}
