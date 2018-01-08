package com.github.nscuro.wdm.factory;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RemoteWebDriverFactoryTest {

    @Nested
    class RequireValidUrlTest {

        @Test
        void shouldReturnUrlValueWhenInputIsValid() {
            assertThat(RemoteWebDriverFactory.requireValidUrl("http://localhost:4444/xyz"))
                    .hasProtocol("http")
                    .hasHost("localhost")
                    .hasPort(4444)
                    .hasPath("/xyz");
        }

        @Test
        void shouldThrowExceptionWhenInputIsInvalid() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> RemoteWebDriverFactory.requireValidUrl("*notvalid#"));
        }

        @Test
        void shouldThrowExceptionWhenNoInputWasProvided() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> RemoteWebDriverFactory.requireValidUrl(null));
        }

    }

}