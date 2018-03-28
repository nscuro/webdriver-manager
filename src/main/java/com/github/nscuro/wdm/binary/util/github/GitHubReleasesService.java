package com.github.nscuro.wdm.binary.util.github;

import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @since 0.2.0
 */
public interface GitHubReleasesService {

    @Nonnull
    List<GitHubRelease> getAllReleases() throws IOException;

    @Nonnull
    File downloadAsset(final GitHubReleaseAsset asset) throws IOException;

    @Nonnull
    static GitHubReleasesService create(final HttpClient httpClient,
                                        final String repositoryOwner,
                                        final String repositoryName) {
        return new GitHubReleasesServiceImpl(httpClient, repositoryOwner, repositoryName);
    }

}
