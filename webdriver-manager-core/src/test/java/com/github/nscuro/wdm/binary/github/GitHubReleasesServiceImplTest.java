package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Platform;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GitHubReleasesServiceImplTest {

    private HttpClient httpClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        httpClient = mock(HttpClient.class);

        objectMapper = mock(ObjectMapper.class);
    }

    @Nested
    class GetReleaseAssetForPlatformTest {

        private GitHubReleasesService releasesService;

        private Platform platform;

        @BeforeEach
        void beforeEach() {
            releasesService = GitHubReleasesService.create(httpClient, objectMapper);

            platform = mock(Platform.class);
        }

        @Test
        void shouldReturnMatchingAsset() {
            final GitHubReleaseAsset asset = new GitHubReleaseAsset();
            asset.setName("my-asset-macosx64.zip");

            final GitHubRelease release = new GitHubRelease();
            release.setAssets(singleton(asset));

            given(platform.getName())
                    .willReturn("macosx64");

            assertThat(releasesService.getReleaseAssetForPlatform(release, platform))
                    .contains(asset);
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoAssetsArePresent() {
            final GitHubRelease release = new GitHubRelease();
            release.setAssets(Collections.emptySet());

            assertThat(releasesService.getReleaseAssetForPlatform(release, platform))
                    .isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoAssetMatchesTheGivenPlatform() {
            final GitHubReleaseAsset asset = new GitHubReleaseAsset();
            asset.setName("my-asset-macosx64.zip");

            final GitHubRelease release = new GitHubRelease();
            release.setAssets(singleton(asset));

            given(platform.getName())
                    .willReturn("windows64");

            assertThat(releasesService.getReleaseAssetForPlatform(release, platform))
                    .isNotPresent();
        }

    }

    @Nested
    class GetApiCredentialsTest {

        @Test
        void shouldReturnEmptyOptionalWhenUserNameAndTokenAreNull() {
            assertThat(createReleaseServiceWithUserNameAndToken(null, null).getApiCredentials())
                    .isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenUserNameIsNull() {
            assertThat(createReleaseServiceWithUserNameAndToken(null, "token").getApiCredentials())
                    .isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenTokenIsNull() {
            assertThat(createReleaseServiceWithUserNameAndToken("username", null).getApiCredentials())
                    .isNotPresent();
        }

        @Test
        void shouldReturnCredentialsWhenUserNameAndTokenAreNonNull() {
            final String userName = "username";
            final String token = "token";

            final Optional<Credentials> credentials = createReleaseServiceWithUserNameAndToken(userName, token).getApiCredentials();

            assertThat(credentials).isPresent();
            assertThat(credentials.get().getUserPrincipal().getName()).isEqualTo(userName);
            assertThat(credentials.get().getPassword()).isEqualTo(token);
        }

        private GitHubReleasesServiceImpl createReleaseServiceWithUserNameAndToken(@Nullable final String userName, @Nullable final String token) {
            return new GitHubReleasesServiceImpl(httpClient, objectMapper, userName, token);
        }

    }

    @Test
    void testBuildRepositoryUrl() {
        assertThat(GitHubReleasesServiceImpl.buildRepositoryUrl("owner", "name"))
                .contains("/repos/owner/name");
    }

}