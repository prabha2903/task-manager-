package com.taskmanager.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. TASK_CREATED, TASK_STATUS_CHANGED, COMMENT_ADDED, GITHUB_REPOSITORY_LINKED */
    private String action;

    /** Human-readable description */
    @Column(length = 1000)
    private String detail;

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password","role"})
    private User user;

    /** Nullable — task-level events */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    @JsonIgnoreProperties({"description","assignedUser","project","createdBy","dueDate"})
    private Task task;

    /** Nullable — project-level events (GitHub link, team change, etc.) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"assignedTeam","githubRepoUrl","githubLinkedAt","description"})
    private Project project;
}
