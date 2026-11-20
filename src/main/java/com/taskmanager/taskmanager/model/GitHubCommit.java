package com.taskmanager.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "github_commits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id","sha"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GitHubCommit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"assignedTeam","githubRepoUrl","githubLinkedAt","description"})
    private Project project;

    @Column(nullable = false, length = 40)
    private String sha;

    @Column(length = 1000)
    private String message;

    private String author;
    private String commitUrl;
    private LocalDateTime commitDate;
    private LocalDateTime syncedAt;

    @PrePersist
    public void prePersist() { syncedAt = LocalDateTime.now(); }
}
