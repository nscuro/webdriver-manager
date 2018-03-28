package com.github.nscuro.wdm.binary.util;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.String.format;

/**
 * HTTP related utility methods.
 */
public final class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Verify that a given {@link HttpResponse} does have any of the given status codes.
     *
     * @param httpResponse The {@link HttpResponse} to verify the status code of
     * @param statusCodes  The status codes to look for
     * @return The status code that matched the actual status code of the {@link HttpResponse}
     * @throws IllegalStateException When none of the given status codes matches the actual one
     */
    public static int verifyStatusCodeIsAnyOf(final HttpResponse httpResponse, final int... statusCodes) {
        final int actualStatusCode = httpResponse.getStatusLine().getStatusCode();

        return Arrays.stream(statusCodes)
                .filter(code -> code == actualStatusCode)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(format("Unexpected status code \"%d\"", actualStatusCode)));
    }

    /**
     * Verify that a given {@link HttpResponse} does define any of the given content types.
     *
     * @param httpResponse The {@link HttpResponse} to verify the content type of
     * @param contentTypes The content types to look for
     * @return The content type that matched the actual content type of the {@link HttpResponse}
     * @throws IllegalStateException When the given {@link HttpResponse} does not define a content type header
     *                               or when none of the given content types matches the actual one
     */
    @Nonnull
    public static String verifyContentTypeIsAnyOf(final HttpResponse httpResponse, final String... contentTypes) {
        final String actualContentType = Optional.ofNullable(httpResponse.getLastHeader(HttpHeaders.CONTENT_TYPE))
                .map(Header::getValue)
                .orElseThrow(() -> new IllegalStateException("HTTP response does not define any content type"));

        return Arrays.stream(contentTypes)
                .filter(contentType -> contentType.equalsIgnoreCase(actualContentType))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(format("Unexpected content type \"%s\"", actualContentType)));
    }

}
