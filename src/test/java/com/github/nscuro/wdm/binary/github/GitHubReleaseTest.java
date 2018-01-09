package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubReleaseTest {

    @Nested
    class JsonDeserializationTest {

        private ObjectMapper objectMapper;

        private String serializedRelease;

        @BeforeEach
        void beforeEach() throws IOException {
            objectMapper = new ObjectMapper();

            try (final InputStream inputStream = getClass().getResourceAsStream("/github/release.json")) {
                serializedRelease = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        }

        @Test
        void testDeserialization() throws IOException {
            final GitHubRelease release = objectMapper.readValue(serializedRelease, GitHubRelease.class);

            assertThat(release.getId()).isEqualTo(1);
            assertThat(release.getTagName()).isEqualTo("v1.0.0");
            assertThat(release.getName()).isEqualTo("v1.0.0");
            assertThat(release.getDraft()).isFalse();
            assertThat(release.getPreRelease()).isFalse();
            assertThat(release.getAssets()).hasSize(1);
        }

    }

}