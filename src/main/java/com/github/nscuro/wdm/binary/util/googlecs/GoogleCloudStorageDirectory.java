package com.github.nscuro.wdm.binary.util.googlecs;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_X_ZIP_COMPRESSED;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.lang.String.format;

/**
 * @since 0.2.0
 */
public class GoogleCloudStorageDirectory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorageDirectory.class);

    private final HttpClient httpClient;

    private final String directoryUrl;

    public GoogleCloudStorageDirectory(final HttpClient httpClient, final String directoryUrl) {
        this.httpClient = httpClient;
        this.directoryUrl = directoryUrl;
    }

    @Nonnull
    public List<GoogleCloudStorageEntry> getEntries() throws IOException {
        final Document directoryDocument = httpClient.execute(new HttpGet(directoryUrl), httpResponse -> {
            try (final InputStream inputStream = httpResponse.getEntity().getContent()) {
                return Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), directoryUrl, Parser.xmlParser());
            }
        });

        return directoryDocument
                .select("Contents")
                .stream()
                .map(entry -> {
                    final Optional<String> key = Optional
                            .ofNullable(entry.selectFirst("Key"))
                            .map(Element::text);

                    final Optional<String> url = key
                            .map(entryKey -> directoryUrl + entryKey);

                    final Optional<Long> size = Optional
                            .ofNullable(entry.selectFirst("Size"))
                            .map(Element::text)
                            .map(text -> {
                                try {
                                    return Long.parseLong(text);
                                } catch (NumberFormatException e) {
                                    return null;
                                }
                            });

                    if (key.isPresent() && size.isPresent()) {
                        return new GoogleCloudStorageEntry(key.get(), url.get(), size.get());
                    } else {
                        LOGGER.warn("Incomplete directory entry: \"{}\"", entry.text());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nonnull
    public File downloadFile(final String fileUrl) throws IOException {
        final String fileName = FilenameUtils.getBaseName(fileUrl);
        final String fileExtension = FilenameUtils.getExtension(fileUrl);

        final HttpGet request = new HttpGet(fileUrl);
        request.setHeader(HttpHeaders.ACCEPT, format("%s,%s", APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED));

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);

            verifyContentTypeIsAnyOf(httpResponse, APPLICATION_ZIP, APPLICATION_X_ZIP_COMPRESSED);

            final Path targetFilePath = Files.createTempFile(format("%s_", fileName), format(".%s", fileExtension));
            LOGGER.debug("Downloading \"{}\" to \"{}\" ({})", fileName, targetFilePath,
                    httpResponse.getFirstHeader(HttpHeaders.CONTENT_LENGTH));

            try (final OutputStream fileOutputStream = Files.newOutputStream(targetFilePath)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Response body was empty"))
                        .writeTo(fileOutputStream);
            }

            return targetFilePath.toFile();
        });
    }

}
