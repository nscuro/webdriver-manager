package com.github.nscuro.wdm.binary;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    @AfterEach
    void afterEach() {
        Optional.ofNullable(downloadedFile).ifPresent(file -> {
            if (!file.delete()) {
                LOGGER.warn("{} was not deleted", file);
            } else {
                LOGGER.info("{} was successfully deleted", file);
            }
        });
    }

}
