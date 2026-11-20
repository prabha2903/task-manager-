package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.Pattern;

public class GitHubLinkRequest {

    /**
     * Must be a valid GitHub URL:
     *   https://github.com/username/repo
     *   http://github.com/username/repo
     *   github.com/username/repo
     */
    @Pattern(
        regexp = "^(https?://)?github\\.com/[\\w.-]+/[\\w.-]+(/.*)?$",
        message = "Must be a valid GitHub repository URL (e.g. https://github.com/user/repo)"
    )
    private String repoUrl;

    public String getRepoUrl()         { return repoUrl; }
    public void   setRepoUrl(String v) { repoUrl = v; }
}
