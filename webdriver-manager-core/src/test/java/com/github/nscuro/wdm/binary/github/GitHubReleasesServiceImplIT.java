package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubReleasesServiceImplIT {

    private GitHubReleasesService releasesService;

    @BeforeEach
    void beforeEach() {
        final HttpClient httpClient = HttpClients.createSystem();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        releasesService = new GitHubReleasesServiceImpl(httpClient, objectMapper);
    }

    @Test
    void testGetLatest() throws IOException {
        assertThat(releasesService.getLatestRelease("OpenMW", "openmw"))
                .isPresent();
    }

    @Test
    void testGetSpecific() throws IOException {
        assertThat(releasesService.getReleaseByTagName("OpenMW", "openmw", "openmw-0.41.0"))
                .isPresent();
    }

}