package com.github.nscuro.wdm.binary;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public abstract class AbstractBinaryProviderIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBinaryProviderIT.class);

    protected HttpClient httpClient;

    protected File downloadedFile;

    @BeforeEach
    protected void beforeEach() {
        httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0")
                .disableAuthCaching()
                .disableCookieManagement()
                .build();
    }

    protected Path getBinaryDestinationPath(final Class testClass) throws IOException {
        return Files.createTempFile(testClass.getSimpleName(), null);
    }

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
