package com.github.nscuro.wdm.factory;

import com.github.nscuro.wdm.binary.BinaryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

class LocalWebDriverFactoryTest {

    private BinaryManager binaryManager;

    private LocalWebDriverFactory webDriverFactory;

    @BeforeEach
    void beforeEach() {
        binaryManager = mock(BinaryManager.class);

        webDriverFactory = new LocalWebDriverFactory(binaryManager);
    }

    @Nested
    class CreateWebDriverTest {

        @Test
        void shouldThrowExceptionWhenNoBrowserNameWasProvided() {
            final MutableCapabilities desiredCapabilities = new MutableCapabilities();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> webDriverFactory.createWebDriver(desiredCapabilities));
        }

    }

}