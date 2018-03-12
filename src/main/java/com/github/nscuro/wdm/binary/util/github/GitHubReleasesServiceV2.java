package com.github.nscuro.wdm.binary.util.github;

import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @since 0.1.5
 */
public interface GitHubReleasesServiceV2 {

    @Nonnull
    List<GitHubRelease> getAllReleases() throws IOException;

    @Nonnull
    Optional<GitHubRelease> getLatestRelease() throws IOException;

    @Nonnull
    Optional<GitHubRelease> getReleaseById(final int id) throws IOException;

    @Nonnull
    Optional<GitHubRelease> getReleaseByTagName(final String tagName) throws IOException;

    @Nonnull
    File downloadAsset(final GitHubReleaseAsset asset) throws IOException;

    @Nonnull
    static GitHubReleasesServiceV2 create(final HttpClient httpClient,
                                          final String repositoryOwner,
                                          final String repositoryName) {
        return new GitHubReleasesServiceV2Impl(httpClient, repositoryOwner, repositoryName);
    }

}
