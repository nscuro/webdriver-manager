package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

public final class GitHubReleasesServiceImpl implements GitHubReleasesService {

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public GitHubReleasesServiceImpl(final HttpClient httpClient,
                                     final ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Nonnull
    @Override
    public Optional<GitHubRelease> getLatestRelease(final String repoOwner, final String repoName) throws IOException {
        final String repositoryUrl = buildRepositoryUrl(repoOwner, repoName);

        final HttpGet request = new HttpGet(repositoryUrl + "/releases/latest");
        request.setHeader(HttpHeaders.ACCEPT, "application/json; charset=utf-8");

        final String jsonResponse = httpClient.execute(request, httpResponse -> {
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    if (httpResponse.getEntity() == null) {
                        throw new IllegalStateException();
                    } else {
                        return EntityUtils.toString(httpResponse.getEntity());
                    }
                case HttpStatus.SC_NOT_FOUND:
                    throw new IllegalStateException();
                default:
                    throw new IllegalStateException();
            }
        });

        return Optional.of(objectMapper.readValue(jsonResponse, GitHubRelease.class));
    }

    @Override
    public Optional<GitHubRelease> getReleaseByTagName(final String repoOwner, final String repoName, final String tagName) throws IOException {
        final String repositoryUrl = buildRepositoryUrl(repoOwner, repoName);

        final HttpGet request = new HttpGet(repositoryUrl + "/releases/tags/" + tagName);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");

        final String jsonResponse = httpClient.execute(request, httpResponse -> {
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    if (httpResponse.getEntity() == null) {
                        throw new IllegalStateException();
                    } else {
                        return EntityUtils.toString(httpResponse.getEntity());
                    }
                case HttpStatus.SC_NOT_FOUND:
                    return null;
                default:
                    throw new IllegalStateException();
            }
        });

        if (jsonResponse != null) {
            return Optional.of(objectMapper.readValue(jsonResponse, GitHubRelease.class));
        } else {
            return Optional.empty();
        }
    }

    static String buildRepositoryUrl(final String repoOwner, final String repoName) {
        return format("https://api.github.com/repos/%s/%s", repoOwner, repoName);
    }

}
