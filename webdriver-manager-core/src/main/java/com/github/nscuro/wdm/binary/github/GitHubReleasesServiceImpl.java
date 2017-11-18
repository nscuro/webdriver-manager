package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

final class GitHubReleasesServiceImpl implements GitHubReleasesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubReleasesServiceImpl.class);

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final String gitHubUserName;

    private final String oAuthToken;

    GitHubReleasesServiceImpl(final HttpClient httpClient,
                              final ObjectMapper objectMapper,
                              @Nullable final String gitHubUserName,
                              @Nullable final String oAuthToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.gitHubUserName = gitHubUserName;
        this.oAuthToken = oAuthToken;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<GitHubRelease> getLatestRelease(final String repoOwner, final String repoName) throws IOException {
        return getGitHubReleaseFromPath("/releases/latest", repoOwner, repoName);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<GitHubRelease> getReleaseByTagName(final String repoOwner, final String repoName, final String tagName) throws IOException {
        return getGitHubReleaseFromPath("/releases/tags/" + tagName, repoOwner, repoName);
    }

    @Nonnull
    private Optional<GitHubRelease> getGitHubReleaseFromPath(final String path, final String repoOwner, final String repoName) throws IOException {
        final HttpGet request = new HttpGet(buildRepositoryUrl(repoOwner, repoName) + path);
        request.setHeader(HttpHeaders.ACCEPT, "application/json; charset=utf-8");

        getApiCredentials().ifPresent(credentials -> {
                    try {
                        request.addHeader(new BasicScheme().authenticate(credentials, request, null));
                        LOGGER.debug("Basic auth credentials attached to request");
                    } catch (AuthenticationException e) {
                        LOGGER.error("Basic auth credentials could not be attached to request");
                    }
                }
        );

        final String jsonResponse = httpClient.execute(request, httpResponse -> {
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    if (httpResponse.getEntity() == null) {
                        throw new IllegalStateException("Response body is empty");
                    } else {
                        return EntityUtils.toString(httpResponse.getEntity());
                    }
                case HttpStatus.SC_NOT_FOUND:
                    return null;
                default:
                    throw new IllegalStateException("Unexpected HTTP status");
            }
        });

        if (jsonResponse != null) {
            return Optional.of(objectMapper.readValue(jsonResponse, GitHubRelease.class));
        } else {
            return Optional.empty();
        }
    }

    @Nonnull
    Optional<Credentials> getApiCredentials() {
        if (gitHubUserName != null && oAuthToken != null) {
            return Optional.of(new UsernamePasswordCredentials(gitHubUserName, oAuthToken));
        } else {
            return Optional.empty();
        }
    }

    @Nonnull
    static String buildRepositoryUrl(final String repoOwner, final String repoName) {
        return format("https://api.github.com/repos/%s/%s", repoOwner, repoName);
    }

}
