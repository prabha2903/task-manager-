package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.enums.TaskStatus;

public class UpdateTaskStatusRequest {

    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}