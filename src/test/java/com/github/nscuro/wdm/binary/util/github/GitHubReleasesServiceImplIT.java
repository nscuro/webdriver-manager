package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class GitHubReleasesServiceImplIT {

    private static final String REPOSITORY_OWNER = "someUser";

    private static final String REPOSITORY_NAME = "someName";

    private static WireMockServer wireMockServer;

    private static String baseUrl;

    private GitHubReleasesServiceImpl gitHubReleasesService;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer();

        wireMockServer.start();

        baseUrl = wireMockServer.url("");
    }

    @BeforeEach
    void beforeEach() {
        gitHubReleasesService = new GitHubReleasesServiceImpl(HttpClients.createDefault(),
                new ObjectMapper(), baseUrl, REPOSITORY_OWNER, REPOSITORY_NAME);
    }

    @Nested
    class GetAllReleasesTest {

        @Test
        void shouldReturnAllReleases() throws IOException, URISyntaxException {
            stubFor(get(urlPathEqualTo(format("/repos/%s/%s/releases", REPOSITORY_OWNER, REPOSITORY_NAME)))
                    .willReturn(aResponse()
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .withBody(getJsonContent("releases.json"))));

            final List<GitHubRelease> releases = gitHubReleasesService.getAllReleases();

            assertThat(releases)
                    .hasSize(2)
                    .flatExtracting(GitHubRelease::getAssets)
                    .hasSize(12);
        }

    }

    @Nested
    class PerformApiRequestTest {

        @Test
        void shouldThrowExceptionWhenStatusIsNotExpected() {
            stubFor(get(urlPathEqualTo(format("/repos/%s/%s/test", REPOSITORY_OWNER, REPOSITORY_NAME)))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> gitHubReleasesService.performApiRequest("/test"));
        }

        @Test
        void shouldThrowExceptionWhenContentTypeIsNotJson() {
            stubFor(get(urlPathEqualTo(format("/repos/%s/%s/test", REPOSITORY_OWNER, REPOSITORY_NAME)))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_OK)
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/xml")));

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> gitHubReleasesService.performApiRequest("/test"));
        }

        @Test
        void shouldThrowExceptionWhenApiRateLimitHasExceeded() {
            stubFor(get(urlPathEqualTo(format("/repos/%s/%s/test", REPOSITORY_OWNER, REPOSITORY_NAME)))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_FORBIDDEN)
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .withHeader("X-RateLimit-Remaining", "0")));

            assertThatExceptionOfType(IOException.class)
                    .isThrownBy(() -> gitHubReleasesService.performApiRequest("/test"))
                    .withMessageContaining("rate limit");
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

    private String getJsonContent(final String jsonFileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(
                Paths.get(getClass().getResource(format("/github/%s", jsonFileName)).toURI())));
    }

}