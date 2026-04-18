package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.CommentRequest;
import com.taskmanager.taskmanager.model.Comment;
import com.taskmanager.taskmanager.service.CommentService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ADD COMMENT
    @PostMapping
    public Comment addComment(@RequestBody CommentRequest request,
                              Principal principal){
        return commentService.addComment(request, principal.getName());
    }

    // GET COMMENTS BY TASK
    @GetMapping("/{taskId}")
    public List<Comment> getComments(@PathVariable Long taskId){
        return commentService.getComments(taskId);
    }
}