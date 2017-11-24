package com.github.nscuro.wdm.binary.util;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class HttpUtilsTest {

    @Nested
    class VerifyContentTypeIsAnyOfTest {

        private HttpResponse httpResponse;

        @BeforeEach
        void beforeEach() {
            httpResponse = mock(HttpResponse.class);
        }

        @Test
        void shouldThrowExceptionWhenResponseHasNoContentTypeHeader() {
            given(httpResponse.getLastHeader(HttpHeaders.CONTENT_TYPE))
                    .willReturn(null);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> HttpUtils.verifyContentTypeIsAnyOf(httpResponse, "any"));
        }

        @Test
        void shouldThrowExceptionWhenExpectedContentTypesDoNotMatchTheActualOne() {
            final Header contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, MimeType.APPLICATION_ZIP);

            given(httpResponse.getLastHeader(HttpHeaders.CONTENT_TYPE))
                    .willReturn(contentTypeHeader);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> HttpUtils.verifyContentTypeIsAnyOf(httpResponse, MimeType.APPLICATION_X_ZIP_COMPRESSED));
        }

        @Test
        void shouldReturnTheMatchedContentType() {
            final Header contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, MimeType.APPLICATION_ZIP);

            given(httpResponse.getLastHeader(HttpHeaders.CONTENT_TYPE))
                    .willReturn(contentTypeHeader);

            assertThat(HttpUtils.verifyContentTypeIsAnyOf(httpResponse, MimeType.APPLICATION_ZIP, MimeType.APPLICATION_X_ZIP_COMPRESSED))
                    .isEqualTo(MimeType.APPLICATION_ZIP);
        }

    }

}