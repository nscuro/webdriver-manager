package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubReleasesServiceImplIT {

    private GitHubReleasesService releasesService;

    @BeforeEach
    void beforeEach() {
        releasesService = GitHubReleasesService.create(HttpClients.createSystem(), new ObjectMapper());
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