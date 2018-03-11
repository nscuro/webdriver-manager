package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

class GitHubReleasesServiceV2ImplTest {

    private static final String REPOSITORY_OWNER = "someUser";

    private static final String REPOSITORY_NAME = "someName";

    private static WireMockServer wireMockServer;

    private static String baseUrl;

    private GitHubReleasesServiceV2 gitHubReleasesService;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer();

        wireMockServer.start();

        baseUrl = wireMockServer.url("");

        System.out.println(baseUrl);
    }

    @BeforeEach
    void beforeEach() {
        gitHubReleasesService = new GitHubReleasesServiceV2Impl(HttpClients.createDefault(),
                new ObjectMapper(), baseUrl, REPOSITORY_OWNER, REPOSITORY_NAME);
    }

    @AfterEach
    void afterEach() {
        WireMock.reset();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

}