package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Platform;
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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_GZIP;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_OCTET_STREAM;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
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

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<GitHubReleaseAsset> getReleaseAssetForPlatform(final GitHubRelease release, final Platform platform) {
        return release.getAssets().stream()
                .filter(asset -> asset.getName().toLowerCase().contains(platform.getName().toLowerCase()))
                .findAny();
    }

    @Nonnull
    @Override
    public File downloadAsset(final GitHubReleaseAsset asset) throws IOException {
        final HttpGet request = new HttpGet(asset.getBrowserDownloadUrl());
        request.setHeader(HttpHeaders.ACCEPT, asset.getContentType());

        final Path targetFilePath = Files.createTempFile(asset.getName(), null);

        LOGGER.debug("Downloading archived binary to {}", targetFilePath);

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK);
            verifyContentTypeIsAnyOf(httpResponse, APPLICATION_ZIP, APPLICATION_GZIP, APPLICATION_OCTET_STREAM);

            try (final OutputStream fileOutputStream = Files.newOutputStream(targetFilePath)) {
                Optional.ofNullable(httpResponse.getEntity())
                        .orElseThrow(() -> new IllegalStateException("Response body was empty"))
                        .writeTo(fileOutputStream);
            }

            return targetFilePath.toFile();
        });
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
