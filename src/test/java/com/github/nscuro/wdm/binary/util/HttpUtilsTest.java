package com.github.nscuro.wdm.binary.util;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpUtilsTest {

    private HttpResponse httpResponse;

    @BeforeEach
    void beforeEach() {
        httpResponse = mock(HttpResponse.class);
    }

    @Nested
    class VerifyStatusCodeIsAnyOfTest {

        private StatusLine statusLine;

        @BeforeEach
        void beforeEach() {
            statusLine = mock(StatusLine.class);

            when(httpResponse.getStatusLine())
                    .thenReturn(statusLine);
        }

        @Test
        void shouldThrowExceptionWhenExpectedStatusCodesDoNotMatchTheActualOne() {
            given(statusLine.getStatusCode())
                    .willReturn(HttpStatus.SC_BAD_REQUEST);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> HttpUtils.verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK));
        }

        @Test
        void shouldReturnTheMatchedStatusCode() {
            given(statusLine.getStatusCode())
                    .willReturn(HttpStatus.SC_NOT_FOUND);

            assertThat(HttpUtils.verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND))
                    .isEqualTo(HttpStatus.SC_NOT_FOUND);
        }

    }

    @Nested
    class VerifyContentTypeIsAnyOfTest {

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

        @Test
        void shouldMatchCaseInsensitive() {
            final Header contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "Application/XML; charset=UTF-8");

            given(httpResponse.getLastHeader(HttpHeaders.CONTENT_TYPE))
                    .willReturn(contentTypeHeader);

            // shouldn't throw an exception
            HttpUtils.verifyContentTypeIsAnyOf(httpResponse, "application/xml; charset=utf-8");
        }

    }

}