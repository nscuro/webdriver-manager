package com.github.nscuro.wdm.binary.util.github;

import javax.annotation.Nonnull;
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

}
