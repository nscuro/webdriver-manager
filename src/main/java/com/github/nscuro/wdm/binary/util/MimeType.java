package com.github.nscuro.wdm.binary.util;

/**
 * MIME types that are not defined in {@link org.apache.http.entity.ContentType}.
 */
public final class MimeType {

    public static final String APPLICATION_ZIP = "application/zip";

    public static final String APPLICATION_X_ZIP_COMPRESSED = "application/x-zip-compressed";

    public static final String APPLICATION_GZIP = "application/gzip";

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static final String APPLICATION_XML = "application/xml";

    public static final String APPLICATION_XML_UTF8 = "application/xml; charset=utf-8";

    public static final String APPLICATION_JSON = "application/json";

    public static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";

    private MimeType() {
    }

}
