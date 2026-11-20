package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.GitHubPullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GitHubPullRequestRepository extends JpaRepository<GitHubPullRequest, Long> {
    List<GitHubPullRequest> findByProjectIdOrderByGithubCreatedAtDesc(Long projectId);
    Optional<GitHubPullRequest> findByProjectIdAndPrNumber(Long projectId, Integer prNumber);
}
