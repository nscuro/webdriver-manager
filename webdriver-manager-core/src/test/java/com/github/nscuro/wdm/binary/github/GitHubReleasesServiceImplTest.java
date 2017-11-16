package com.github.nscuro.wdm.binary.github;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubReleasesServiceImplTest {

    @Test
    void testBuildRepositoryUrl() {
        assertThat(GitHubReleasesServiceImpl.buildRepositoryUrl("owner", "name"))
                .contains("/repos/owner/name");
    }

}