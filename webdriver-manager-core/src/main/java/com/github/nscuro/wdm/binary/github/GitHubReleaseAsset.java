package com.github.nscuro.wdm.binary.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

}
