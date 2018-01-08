package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nscuro.wdm.Platform;
import org.apache.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * A minimal service for retrieval of {@link GitHubRelease}s.
 *
 * @see <a href="https://developer.github.com/v3/repos/releases/">REST API documentation</a>
 */
public interface GitHubReleasesService {

    String ENV_GITHUB_USERNAME = "WDM_GH_USER";

    String ENV_GITHUB_APITOKEN = "WDM_GH_TOKEN";

    /**
     * Get the latest release.
     *
     * @param repoOwner Owner of the repository to get the latest release from
     * @param repoName  Name of the repository to get the latest release from
     * @return An {@link Optional} containing the latest {@link GitHubRelease}
     *         or nothing when no {@link GitHubRelease} exists for the given repository owner and name
     * @throws IOException When an error occured while communicating with remote
     * @see <a href="https://developer.github.com/v3/repos/releases/#get-the-latest-release">API endpoint documentation</a>
     */
    @Nonnull
    Optional<GitHubRelease> getLatestRelease(final String repoOwner, final String repoName) throws IOException;

    /**
     * Get a specific {@link GitHubRelease} by its tag name.
     *
     * @param repoOwner Owner of the repository to get the latest release from
     * @param repoName  Name of the repository to get the latest release from
     * @param tagName   The releases tag name (this is typically the version of the release)
     * @return An {@link Optional} containing the requested {@link GitHubRelease}
     *         or nothing when the given tag name does not exist
     * @throws IOException When an error occured while communicating with remote
     * @see <a href="https://developer.github.com/v3/repos/releases/#get-a-release-by-tag-name">API endpoint documentation</a>
     */
    @Nonnull
    Optional<GitHubRelease> getReleaseByTagName(final String repoOwner, final String repoName, final String tagName) throws IOException;

    /**
     * Get a {@link GitHubReleaseAsset} from a {@link GitHubRelease} for a given {@link Platform}.
     *
     * @param release  The {@link GitHubRelease} to get the {@link GitHubReleaseAsset} from
     * @param platform The desired {@link Platform}
     * @return The corresponding {@link GitHubReleaseAsset} or {@link Optional#empty()} when no such
     *         {@link GitHubReleaseAsset} has been found
     */
    @Nonnull
    Optional<GitHubReleaseAsset> getReleaseAssetForPlatform(final GitHubRelease release, final Platform platform);

    /**
     * Download the given {@link GitHubReleaseAsset} to the system's temp directory.
     *
     * @param asset The asset to download
     * @return A {@link File} handle of the downloaded file
     * @throws IOException When the download failed
     */
    @Nonnull
    File downloadAsset(final GitHubReleaseAsset asset) throws IOException;

    static GitHubReleasesService create(final HttpClient httpClient, final ObjectMapper objectMapper) {
        return new GitHubReleasesServiceImpl(httpClient, objectMapper,
                System.getenv(ENV_GITHUB_USERNAME), System.getenv(ENV_GITHUB_APITOKEN));
    }

    static GitHubReleasesService create(final HttpClient httpClient) {
        return create(httpClient, new ObjectMapper());
    }

}
