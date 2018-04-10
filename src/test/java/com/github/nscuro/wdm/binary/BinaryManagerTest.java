package com.github.nscuro.wdm.binary;

import com.github.nscuro.wdm.binary.chrome.ChromeDriverBinaryProvider;
import com.github.nscuro.wdm.binary.edge.MicrosoftWebDriverBinaryProvider;
import com.github.nscuro.wdm.binary.firefox.GeckoDriverBinaryProvider;
import com.github.nscuro.wdm.binary.ie.IEDriverServerBinaryProvider;
import com.github.nscuro.wdm.binary.opera.OperaChromiumDriverBinaryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryManagerTest {

    @Nested
    class CreateDefaultTest {

        BinaryManagerImpl binaryManager;

        @BeforeEach
        void beforeEach() {
            binaryManager = (BinaryManagerImpl) BinaryManager.createDefault();
        }

        @Test
        void shouldIncludeAllBuiltinBinaryProviders() {
            assertThat(binaryManager.getBinaryProviders())
                    .hasAtLeastOneElementOfType(ChromeDriverBinaryProvider.class)
                    .hasAtLeastOneElementOfType(MicrosoftWebDriverBinaryProvider.class)
                    .hasAtLeastOneElementOfType(GeckoDriverBinaryProvider.class)
                    .hasAtLeastOneElementOfType(IEDriverServerBinaryProvider.class)
                    .hasAtLeastOneElementOfType(OperaChromiumDriverBinaryProvider.class);
        }

    }

}