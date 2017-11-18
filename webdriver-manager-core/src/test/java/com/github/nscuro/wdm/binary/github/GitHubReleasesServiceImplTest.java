package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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