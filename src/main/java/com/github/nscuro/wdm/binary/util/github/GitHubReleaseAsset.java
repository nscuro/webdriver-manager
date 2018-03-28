package com.github.nscuro.wdm.binary.util.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nscuro.wdm.Platform;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GitHubReleaseAsset {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("browser_download_url")
    private String browserDownloadUrl;

    @JsonIgnore
    public boolean isAssetForPlatform(final Platform platform) {
        return name.toLowerCase().contains(platform.getName().toLowerCase());
    }

}
