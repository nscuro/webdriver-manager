package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Architecture;
import com.github.nscuro.wdm.Os;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class AbstractBinaryDownloaderIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBinaryDownloaderIT.class);

    protected static final Path DOWNLOAD_DESTINATION_DIR_PATH = Paths.get(System.getProperty("java.io.tmpdir"));

    @Getter(AccessLevel.PROTECTED)
    private HttpClient httpClient;

    protected File downloadedFile;

    @BeforeEach
    protected void beforeEach() {
        httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0")
                .disableAuthCaching()
                .disableCookieManagement()
                .build();
    }

    /**
     * Every {@link BinaryDownloader} should be tested on its "real-life functionality"
     * of downloading a <strong>specific</strong> binary version.
     * <p>
     * It doesn't really matter which {@link Os} and {@link Architecture} is used here,
     * picking one that seems like it's fully supported is fine.
     */
    protected abstract void testDownloadSpecificVersion() throws IOException;

    /**
     * Every {@link BinaryDownloader} should be tested on its "real-life functionality"
     * of downloading the <strong>latest</strong> binary version.
     * <p>
     * It doesn't really matter which {@link Os} and {@link Architecture} is used here,
     * picking one that seems like it's fully supported is fine.
     */
    protected abstract void testDownloadLatest() throws IOException;

    @AfterEach
    void afterEach() {
        Optional.ofNullable(downloadedFile).ifPresent(file -> {
            if (!file.delete()) {
                LOGGER.warn("{} was not deleted", file);
            } else {
                LOGGER.debug("{} was successfully deleted", file);
            }
        });
    }

}
