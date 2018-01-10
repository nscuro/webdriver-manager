package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.Browser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BinaryManagerImplIT {

    private BinaryManager binaryManager;

    private File downloadedFile;

    @BeforeEach
    void beforeEach() {
        binaryManager = BinaryManager.createDefault();
    }

    @Nested
    class GetBinaryTest {

        @ParameterizedTest
        @EnumSource(Browser.class)
        void testGetLatestBinaryForCurrentOsAndArchitecture(final Browser browser) throws IOException {
            assumeTrue(browser.doesRequireBinary());

            downloadedFile = binaryManager.getBinary(browser);

            assertThat(downloadedFile).exists();
            assertThat(downloadedFile.canExecute())
                    .as("The downloaded binary must be executable")
                    .isTrue();
        }

    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(downloadedFile).ifPresent(File::delete);
    }

}