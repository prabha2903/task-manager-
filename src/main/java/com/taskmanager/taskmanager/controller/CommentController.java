package com.taskmanager.taskmanager.controller;
import org.springframework.http.ResponseEntity;
import com.taskmanager.taskmanager.dto.CommentRequest;
import com.taskmanager.taskmanager.model.Comment;
import com.taskmanager.taskmanager.service.CommentService;
import jakarta.validation.Valid;
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
    public Comment addComment(@Valid @RequestBody CommentRequest request,
                              Principal principal){
        return commentService.addComment(request, principal.getName());
    }

    // GET COMMENTS BY TASK
    @GetMapping("/{taskId}")
    public List<Comment> getComments(@PathVariable Long taskId){
        return commentService.getComments(taskId);
    }
    @PutMapping("/{id}")
    public Comment updateComment(@PathVariable Long id,
                                 @RequestBody CommentRequest req,
                                 Principal principal) {

        return commentService.updateComment(
                id,
                req.getContent(),
                principal.getName()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id,
                                              Principal principal) {

        commentService.deleteComment(
                id,
                principal.getName()
        );

        return ResponseEntity.noContent().build();
    }
}