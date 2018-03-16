package com.github.nscuro.wdm.binary.util.googlecs;

import com.github.nscuro.wdm.binary.util.MimeType;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class GoogleCloudStorageDirectoryServiceImplIT {

    private static WireMockServer wireMockServer;

    private static String directoryUrl;

    private GoogleCloudStorageDirectoryServiceImpl googleCloudStorageDirectoryService;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer();

        wireMockServer.start();

        directoryUrl = wireMockServer.url("");
    }

    @BeforeEach
    void beforeEach() {
        googleCloudStorageDirectoryService =
                new GoogleCloudStorageDirectoryServiceImpl(HttpClients.createDefault(), directoryUrl);
    }

    @Nested
    class GetEntriesTest {

        @Test
        void shouldReturnAllEntries() throws IOException, URISyntaxException {
            stubFor(get(urlPathEqualTo("/"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_OK)
                            .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_XML.getMimeType())
                            .withBody(loadDirectoryXml("directory.xml"))));

            final List<GoogleCloudStorageEntry> entries = googleCloudStorageDirectoryService.getEntries();

            assertThat(entries)
                    .extracting(GoogleCloudStorageEntry::getKey)
                    .containsExactly("folder0/somefile.txt", "folder1/someotherfile.md");

            assertThat(entries)
                    .extracting(GoogleCloudStorageEntry::getUrl)
                    .containsExactly(
                            format("%sfolder0/somefile.txt", directoryUrl),
                            format("%sfolder1/someotherfile.md", directoryUrl));

            verify(getRequestedFor(urlPathEqualTo("/"))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(format("%s, %s", MimeType.APPLICATION_XML, MimeType.APPLICATION_XML_UTF8))));
        }

        @Test
        void shouldThrowExceptionWhenResponseStatusIsNotOk() {
            stubFor(get(urlPathEqualTo("/"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_BAD_REQUEST)));

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> googleCloudStorageDirectoryService.getEntries());
        }

        @Test
        void shouldThrowExceptionWhenContentTypeIsNotXml() {
            stubFor(get(urlPathEqualTo("/"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_OK)
                            .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())));

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> googleCloudStorageDirectoryService.getEntries());
        }

    }

    @AfterEach
    void afterEach() {
        WireMock.reset();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    private String loadDirectoryXml(final String xmlFileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(
                Paths.get(getClass().getResource(format("/googlecs/%s", xmlFileName)).toURI())));
    }

}