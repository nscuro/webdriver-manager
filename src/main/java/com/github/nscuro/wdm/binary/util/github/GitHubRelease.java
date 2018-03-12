package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nscuro.wdm.Platform;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GitHubRelease {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("draft")
    private Boolean draft;

    @JsonProperty("prerelease")
    private Boolean preRelease;

    @JsonProperty("assets")
    private Set<GitHubReleaseAsset> assets;

    @JsonIgnore
    public boolean hasAssetForPlatform(final Platform platform) {
        return getAssets()
                .stream()
                .anyMatch(asset -> asset.getName().toLowerCase().contains(platform.getName().toLowerCase()));
    }

}
