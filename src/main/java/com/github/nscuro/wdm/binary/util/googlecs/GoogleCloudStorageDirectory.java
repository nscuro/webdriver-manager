package com.github.nscuro.wdm.binary.util.googlecs;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @since 0.1.5
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
                            .map(Long::parseLong);

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

}
