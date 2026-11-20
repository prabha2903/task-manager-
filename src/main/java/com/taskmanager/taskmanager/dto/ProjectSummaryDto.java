package com.taskmanager.taskmanager.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProjectSummaryDto {
    private Long   projectId;
    private String projectName;
    private long   totalTasks;
    private long   completedTasks;
    private long   backlogTasks;
    private long   inProgressTasks;
    private long   reviewTasks;
    private long   qaTasks;
    private long   blockedTasks;
    private double progress;
    private int    teamSize;
    private String teamName;
    private String teamLeadName;
    private String githubRepoUrl;
    private String githubLinkedAt;
}
