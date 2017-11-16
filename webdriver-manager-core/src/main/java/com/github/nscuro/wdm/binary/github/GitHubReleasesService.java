package com.github.nscuro.wdm.binary.github;

import java.io.IOException;
import java.util.Optional;

/**
 * A minimal service for retrieval of {@link GitHubRelease}s.
 *
 * @see <a href="https://developer.github.com/v3/repos/releases/">REST API documentation</a>
 */
public interface GitHubReleasesService {

    /**
     * Get the latest release.
     *
     * @param repoOwner Owner of the repository to get the latest release from
     * @param repoName  Name of the repository to get the latest release from
     * @return An {@link Optional} containing the latest {@link GitHubRelease}
     *         or nothing when no {@link GitHubRelease} exists for the given repository owner and name
     * @throws IOException
     * @see <a href="https://developer.github.com/v3/repos/releases/#get-the-latest-release">API endpoint documentation</a>
     */
    Optional<GitHubRelease> getLatestRelease(final String repoOwner, final String repoName) throws IOException;

    /**
     * Get a specific {@link GitHubRelease} by its tag name.
     *
     * @param repoOwner Owner of the repository to get the latest release from
     * @param repoName  Name of the repository to get the latest release from
     * @param tagName   The releases tag name (this is typically the version of the release)
     * @return An {@link Optional} containing the requested {@link GitHubRelease}
     *         or nothing when the given tag name does not exist
     * @throws IOException
     * @see <a href="https://developer.github.com/v3/repos/releases/#get-a-release-by-tag-name">API endpoint documentation</a>
     */
    Optional<GitHubRelease> getReleaseByTagName(final String repoOwner, final String repoName, final String tagName) throws IOException;

}
