package com.taskmanager.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "github_pull_requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id","pr_number"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GitHubPullRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"assignedTeam","githubRepoUrl","githubLinkedAt","description"})
    private Project project;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(length = 500)
    private String title;

    private String author;

    /** OPEN, MERGED, CLOSED */
    private String prStatus;

    private String prUrl;
    private LocalDateTime githubCreatedAt;
    private LocalDateTime githubUpdatedAt;
    private LocalDateTime syncedAt;

    @PrePersist
    public void prePersist() { syncedAt = LocalDateTime.now(); }
}
