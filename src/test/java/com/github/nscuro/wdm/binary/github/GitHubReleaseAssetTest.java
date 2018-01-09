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

class GitHubReleaseAssetTest {

    @Nested
    class JsonDeserializationTest {

        private ObjectMapper objectMapper;

        private String serializedAsset;

        @BeforeEach
        void beforeEach() throws IOException {
            objectMapper = new ObjectMapper();

            try (final InputStream inputStream = getClass().getResourceAsStream("/github/asset.json")) {
                serializedAsset = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        }

        @Test
        void testDeserialization() throws IOException {
            final GitHubReleaseAsset asset = objectMapper.readValue(serializedAsset, GitHubReleaseAsset.class);

            assertThat(asset.getId()).isEqualTo(1);
            assertThat(asset.getName()).isEqualTo("example.zip");
            assertThat(asset.getContentType()).isEqualTo("application/zip");
            assertThat(asset.getBrowserDownloadUrl())
                    .isEqualTo("https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip");
        }

    }

}