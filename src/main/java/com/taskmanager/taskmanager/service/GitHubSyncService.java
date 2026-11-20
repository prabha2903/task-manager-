package com.taskmanager.taskmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.model.*;
import com.taskmanager.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GitHub API sync — fetches commits and pull requests.
 * Uses GITHUB_TOKEN env var for authenticated requests (higher rate limits).
 * Falls back gracefully if token is absent.
 * NEVER fails TaskFlow operations — exceptions are caught and logged.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubSyncService {

    private final GitHubCommitRepository      commitRepository;
    private final GitHubPullRequestRepository prRepository;
    private final ProjectRepository           projectRepository;
    private final ActivityLogService          activityLogService;

    @Value("${github.token:}")
    private String githubToken;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient   httpClient   = HttpClient.newHttpClient();

    private static final DateTimeFormatter GH_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // ── Extract owner/repo from URL ───────────────────────────────────────────
    private String[] extractOwnerRepo(String repoUrl) {
        String clean = repoUrl
            .replace("https://github.com/", "")
            .replace("http://github.com/", "")
            .replace("github.com/", "")
            .replaceAll("/$", "");
        String[] parts = clean.split("/");
        if (parts.length < 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Invalid GitHub repository URL");
        return new String[]{parts[0], parts[1].split("\\.git")[0]};
    }

    // ── HTTP call to GitHub API ───────────────────────────────────────────────
    private JsonNode ghGet(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com" + path))
            .header("Accept", "application/vnd.github.v3+json")
            .GET();

        if (githubToken != null && !githubToken.isBlank()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }

        HttpResponse<String> resp = httpClient.send(builder.build(),
            HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 404) throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "GitHub repository not found or private");
        if (resp.statusCode() == 403) throw new ResponseStatusException(
            HttpStatus.TOO_MANY_REQUESTS, "GitHub API rate limit exceeded");
        if (resp.statusCode() != 200) throw new RuntimeException(
            "GitHub API error: " + resp.statusCode());

        return objectMapper.readTree(resp.body());
    }

    // ── Sync commits ──────────────────────────────────────────────────────────
    public List<GitHubCommit> syncCommits(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getGithubRepoUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No GitHub repository linked to this project");
        }

        String[] ownerRepo = extractOwnerRepo(project.getGithubRepoUrl());
        try {
            JsonNode arr = ghGet("/repos/" + ownerRepo[0] + "/" + ownerRepo[1] + "/commits?per_page=20");
            List<GitHubCommit> result = new ArrayList<>();

            for (JsonNode item : arr) {
                String sha = item.path("sha").asText();
                if (commitRepository.findByProjectIdAndSha(projectId, sha).isPresent()) continue;

                JsonNode commitNode  = item.path("commit");
                JsonNode authorNode  = commitNode.path("author");
                String  message      = commitNode.path("message").asText();
                String  authorName   = authorNode.path("name").asText();
                String  dateStr      = authorNode.path("date").asText();
                String  htmlUrl      = item.path("html_url").asText();

                LocalDateTime date = null;
                try { date = LocalDateTime.parse(dateStr, GH_FMT); } catch (Exception ignored) {}

                GitHubCommit commit = GitHubCommit.builder()
                    .project(project)
                    .sha(sha)
                    .message(message.length() > 1000 ? message.substring(0, 1000) : message)
                    .author(authorName)
                    .commitUrl(htmlUrl)
                    .commitDate(date)
                    .build();

                result.add(commitRepository.save(commit));
            }
            log.info("✅ Synced {} new commits for project {}", result.size(), projectId);
            return result;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("GitHub commit sync failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "GitHub sync failed: " + e.getMessage());
        }
    }

    // ── Sync pull requests ────────────────────────────────────────────────────
    public List<GitHubPullRequest> syncPullRequests(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getGithubRepoUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No GitHub repository linked to this project");
        }

        String[] ownerRepo = extractOwnerRepo(project.getGithubRepoUrl());
        try {
            JsonNode arr = ghGet("/repos/" + ownerRepo[0] + "/" + ownerRepo[1] +
                "/pulls?state=all&per_page=20");
            List<GitHubPullRequest> result = new ArrayList<>();

            for (JsonNode item : arr) {
                int prNum = item.path("number").asInt();
                var existing = prRepository.findByProjectIdAndPrNumber(projectId, prNum);

                GitHubPullRequest pr;
                String merged = item.path("merged_at").asText(null);
                String state  = item.path("state").asText("open");
                String prStatus = merged != null ? "MERGED" : state.equalsIgnoreCase("closed") ? "CLOSED" : "OPEN";

                if (existing.isPresent()) {
                    // Update status
                    pr = existing.get();
                    pr.setPrStatus(prStatus);
                } else {
                    pr = GitHubPullRequest.builder()
                        .project(project)
                        .prNumber(prNum)
                        .title(item.path("title").asText())
                        .author(item.path("user").path("login").asText())
                        .prStatus(prStatus)
                        .prUrl(item.path("html_url").asText())
                        .build();
                }

                try {
                    String c = item.path("created_at").asText();
                    pr.setGithubCreatedAt(LocalDateTime.parse(c, GH_FMT));
                } catch (Exception ignored) {}
                try {
                    String u = item.path("updated_at").asText();
                    pr.setGithubUpdatedAt(LocalDateTime.parse(u, GH_FMT));
                } catch (Exception ignored) {}

                result.add(prRepository.save(pr));
            }
            log.info("✅ Synced {} PRs for project {}", result.size(), projectId);
            return result;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("GitHub PR sync failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "GitHub PR sync failed: " + e.getMessage());
        }
    }

    // ── Get stored commits ────────────────────────────────────────────────────
    public List<GitHubCommit> getCommits(Long projectId) {
        return commitRepository.findByProjectIdOrderByCommitDateDesc(projectId);
    }

    public List<GitHubPullRequest> getPullRequests(Long projectId) {
        return prRepository.findByProjectIdOrderByGithubCreatedAtDesc(projectId);
    }

    // ── Webhook event handler ─────────────────────────────────────────────────
    public void handleWebhook(Long projectId, String event, String payload) {
        try {
            JsonNode body = objectMapper.readTree(payload);
            if ("push".equals(event)) {
                JsonNode commits = body.path("commits");
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null) return;

                for (JsonNode c : commits) {
                    String sha = c.path("id").asText();
                    if (commitRepository.findByProjectIdAndSha(projectId, sha).isPresent()) continue;

                    GitHubCommit commit = GitHubCommit.builder()
                        .project(project)
                        .sha(sha)
                        .message(c.path("message").asText())
                        .author(c.path("author").path("name").asText())
                        .commitUrl(c.path("url").asText())
                        .commitDate(LocalDateTime.now())
                        .build();
                    commitRepository.save(commit);
                    log.info("Webhook: saved commit {} for project {}", sha, projectId);
                }
            } else if ("pull_request".equals(event)) {
                // Delegate to full sync for simplicity
                syncPullRequests(projectId);
            }
        } catch (Exception e) {
            log.warn("Webhook processing failed: {}", e.getMessage());
        }
    }
}
