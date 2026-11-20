package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.GitHubCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GitHubCommitRepository extends JpaRepository<GitHubCommit, Long> {
    List<GitHubCommit> findByProjectIdOrderByCommitDateDesc(Long projectId);
    Optional<GitHubCommit> findByProjectIdAndSha(Long projectId, String sha);
}
