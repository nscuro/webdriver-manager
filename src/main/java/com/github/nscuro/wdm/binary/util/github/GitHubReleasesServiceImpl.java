package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyContentTypeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.HttpUtils.verifyStatusCodeIsAnyOf;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_GZIP;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_OCTET_STREAM;
import static com.github.nscuro.wdm.binary.util.MimeType.APPLICATION_ZIP;
import static java.lang.String.format;

/**
 * @since 0.1.5
 */
final class GitHubReleasesServiceImpl implements GitHubReleasesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubReleasesServiceImpl.class);

    private static final String GITHUB_USERNAME = System.getenv("WDM_GH_USER");

    private static final String GITHUB_API_TOKEN = System.getenv("WDM_GH_TOKEN");

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final String repositoryUrl;

    GitHubReleasesServiceImpl(final HttpClient httpClient,
                              final String repositoryOwner,
                              final String repositoryName) {
        this(httpClient, new ObjectMapper(), "https://api.github.com/", repositoryOwner, repositoryName);
    }

    GitHubReleasesServiceImpl(final HttpClient httpClient,
                              final ObjectMapper objectMapper,
                              final String baseUrl,
                              final String repositoryOwner,
                              final String repositoryName) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.repositoryUrl = getRepositoryUrl(baseUrl, repositoryOwner, repositoryName);
    }

    @Nonnull
    @Override
    public List<GitHubRelease> getAllReleases() throws IOException {
        return objectMapper.readValue(performApiRequest("/releases"), objectMapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, GitHubRelease.class));
    }

    @Nonnull
    @Override
    public Optional<GitHubRelease> getLatestRelease() throws IOException {
        try {
            return Optional.of(objectMapper.readValue(performApiRequest("/releases/latest"), GitHubRelease.class));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Optional<GitHubRelease> getReleaseById(final int id) throws IOException {
        try {
            return Optional.of(objectMapper.readValue(performApiRequest(format("/releases/%d", id)), GitHubRelease.class));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Optional<GitHubRelease> getReleaseByTagName(final String tagName) throws IOException {
        try {
            return Optional.of(objectMapper.readValue(performApiRequest(format("/releases/%s", tagName)), GitHubRelease.class));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public File downloadAsset(final GitHubReleaseAsset asset) throws IOException {
        final String assetFileName = FilenameUtils.getBaseName(asset.getBrowserDownloadUrl());

        final String assetFileExtension = FilenameUtils.getExtension(asset.getBrowserDownloadUrl());

        final HttpGet request = new HttpGet(asset.getBrowserDownloadUrl());
        request.setHeader(HttpHeaders.ACCEPT, asset.getContentType());

        final Path targetFilePath = Files.createTempFile(format("%s_", assetFileName), format(".%s", assetFileExtension));

        LOGGER.debug("Downloading {}.{} to {}", assetFileName, assetFileExtension, targetFilePath);

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
    private String performApiRequest(final String path) throws IOException {
        final HttpGet request = new HttpGet(format("%s%s", repositoryUrl, path));
        request.setHeader(HttpHeaders.ACCEPT, "application/json; charset=utf-8");

        getApiCredentials().ifPresent(credentials -> {
            try {
                request.addHeader(new BasicScheme().authenticate(credentials, request, null));
                LOGGER.debug("Basic auth credentials attached to API request");
            } catch (AuthenticationException e) {
                LOGGER.error("Basic auth credentials could not be attached to API request:\n{}", e);
            }
        });

        return httpClient.execute(request, httpResponse -> {
            verifyStatusCodeIsAnyOf(httpResponse, HttpStatus.SC_OK, HttpStatus.SC_FORBIDDEN, HttpStatus.SC_NOT_FOUND);
            verifyContentTypeIsAnyOf(httpResponse, "application/json", "application/json; charset=utf-8");

            final Optional<Integer> remainingRateLimit = Optional
                    .ofNullable(httpResponse.getFirstHeader("X-RateLimit-Remaining"))
                    .map(Header::getValue)
                    .map(Integer::parseInt);

            final Optional<LocalDateTime> rateLimitResetTime = Optional
                    .ofNullable(httpResponse.getFirstHeader("X-RateLimit-Reset"))
                    .map(Header::getValue)
                    .map(Long::parseLong)
                    .map(Instant::ofEpochSecond)
                    .map(instant -> LocalDateTime.ofInstant(instant, ZoneOffset.UTC));

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN
                    && rateLimitResetTime.isPresent()) {
                throw new IOException(format("Request was rejected because your GitHub API rate limit is exceeded. "
                        + "It will be reset at %s UTC time", rateLimitResetTime.get()));
            } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new NoSuchElementException();
            }

            if (remainingRateLimit.isPresent() && remainingRateLimit.get() <= 10 && rateLimitResetTime.isPresent()) {
                LOGGER.warn("You have only {} requests left until GitHub's API rate limit kicks in. "
                        + "It will be reset at {} UTC time", remainingRateLimit.get(), rateLimitResetTime.get());
            }

            return EntityUtils.toString(httpResponse.getEntity());
        });
    }

    @Nonnull
    Optional<Credentials> getApiCredentials() {
        if (GITHUB_USERNAME != null && GITHUB_API_TOKEN != null) {
            return Optional.of(new UsernamePasswordCredentials(GITHUB_USERNAME, GITHUB_API_TOKEN));
        } else {
            return Optional.empty();
        }
    }

    @Nonnull
    private String getRepositoryUrl(final String baseUrl, final String repositoryOwner, final String repositoryName) {
        return format("%srepos/%s/%s", baseUrl, repositoryOwner, repositoryName);
    }

}
