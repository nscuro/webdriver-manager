package com.github.nscuro.wdm.binary.util.github;

import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

/**
 * A service for accessing GitHub's API for releases.
 *
 * @see <a href="https://developer.github.com/v3/repos/releases/">API documentation</a>
 * @since 0.2.0
 */
public interface GitHubReleasesService {

    /**
     * Get all available {@link GitHubRelease}s of the repository.
     *
     * @return All available {@link GitHubRelease}s
     * @throws IOException In case of a networking error
     */
    @Nonnull
    List<GitHubRelease> getAllReleases() throws IOException;

    /**
     * Download a given {@link GitHubReleaseAsset}.
     *
     * @param asset The {@link GitHubReleaseAsset} to download
     * @return The downloaded {@link File}
     * @throws IOException In case of a networking error
     * @see Files#createTempFile(String, String, FileAttribute[])
     */
    @Nonnull
    File downloadAsset(final GitHubReleaseAsset asset) throws IOException;

    /**
     * Create a new {@link GitHubReleasesService} instance.
     *
     * @param httpClient      The {@link HttpClient} to use
     * @param repositoryOwner Owner of the GitHub repository
     * @param repositoryName  Name of the GitHub repository
     * @return A new {@link GitHubReleasesService} instance
     */
    @Nonnull
    static GitHubReleasesService create(final HttpClient httpClient,
                                        final String repositoryOwner,
                                        final String repositoryName) {
        return new GitHubReleasesServiceImpl(httpClient, repositoryOwner, repositoryName);
    }

}
