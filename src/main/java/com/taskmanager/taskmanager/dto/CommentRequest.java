package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Comment content is required")
    private String content;
}