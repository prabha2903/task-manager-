package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 🔥 ADD THIS METHOD
    List<Comment> findByTaskId(Long taskId);
}