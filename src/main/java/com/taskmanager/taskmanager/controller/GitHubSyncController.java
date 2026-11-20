package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.GitHubCommit;
import com.taskmanager.taskmanager.model.GitHubPullRequest;
import com.taskmanager.taskmanager.service.GitHubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class GitHubSyncController {

    private final GitHubSyncService gitHubSyncService;

    /** Fetch stored commits (no live sync) */
    @GetMapping("/{projectId}/commits")
    public List<GitHubCommit> getCommits(@PathVariable Long projectId) {
        return gitHubSyncService.getCommits(projectId);
    }

    /** Fetch stored pull requests */
    @GetMapping("/{projectId}/pulls")
    public List<GitHubPullRequest> getPullRequests(@PathVariable Long projectId) {
        return gitHubSyncService.getPullRequests(projectId);
    }

    /** Manually trigger commit sync from GitHub API */
    @PostMapping("/{projectId}/sync/commits")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Map<String, Object> syncCommits(@PathVariable Long projectId) {
        List<GitHubCommit> synced = gitHubSyncService.syncCommits(projectId);
        return Map.of("synced", synced.size(), "message", "Commits synced successfully");
    }

    /** Manually trigger PR sync from GitHub API */
    @PostMapping("/{projectId}/sync/pulls")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Map<String, Object> syncPullRequests(@PathVariable Long projectId) {
        List<GitHubPullRequest> synced = gitHubSyncService.syncPullRequests(projectId);
        return Map.of("synced", synced.size(), "message", "Pull requests synced successfully");
    }

    /** GitHub webhook receiver — validates event and processes */
    @PostMapping("/{projectId}/webhook/github")
    public Map<String, String> webhook(@PathVariable Long projectId,
                                       @RequestHeader(value = "X-GitHub-Event", defaultValue = "push") String event,
                                       @RequestBody String payload) {
        gitHubSyncService.handleWebhook(projectId, event, payload);
        return Map.of("status", "received");
    }
}
