package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.enums.TaskPriority;
import com.taskmanager.taskmanager.model.enums.TaskStatus;

import java.time.LocalDate;

public class CreateTaskRequest {

    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate dueDate;

    private Long assignedUserId;
    private Long projectId;

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public TaskPriority getPriority() { return priority; }

    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getAssignedUserId() { return assignedUserId; }

    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public Long getProjectId() { return projectId; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
}