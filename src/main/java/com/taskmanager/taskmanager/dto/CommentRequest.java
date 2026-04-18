package com.taskmanager.taskmanager.dto;
import lombok.Data;
@Data
public class CommentRequest {
    private Long taskId;
    private String content;
}
