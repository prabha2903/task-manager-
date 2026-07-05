package com.taskmanager.taskmanager.service;
import com.taskmanager.taskmanager.dto.CommentRequest;
import com.taskmanager.taskmanager.model.Comment;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.CommentRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService; // 🔥 NEW

    // 🔥 Add Comment
    public Comment addComment(CommentRequest request, String email){

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Comment content cannot be empty");
        }

        // ✅ Task check
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"));

        // ✅ User check
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // ✅ Create comment
        Comment comment = Comment.builder()
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .task(task)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 🔥 ACTIVITY LOG (VERY IMPORTANT)
        activityLogService.log(
                user,
                "COMMENT_ADDED",
                request.getContent(),
                task
        );

        return savedComment;
    }

    // 🔥 Get Comments
    public List<Comment> getComments(Long taskId){

        if(taskId == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Task ID cannot be null");
        }

        if(!taskRepository.existsById(taskId)){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Task not found");
        }

        return commentRepository.findByTaskId(taskId);
    }

    public Comment updateComment(Long id, String content, String email) {

        Comment c = commentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Comment not found"
                        ));

        if (!c.getUser().getEmail().equals(email)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not your comment"
            );
        }

        c.setContent(content);

        return commentRepository.save(c);
    }

    public void deleteComment(Long id, String email) {

        Comment c = commentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Comment not found"
                        ));

        if (!c.getUser().getEmail().equals(email)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not your comment"
            );
        }

        commentRepository.delete(c);
    }
}