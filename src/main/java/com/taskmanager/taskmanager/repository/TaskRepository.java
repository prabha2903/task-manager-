package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.enums.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // ✅ Get tasks by assigned user
    List<Task> findByAssignedUser(User user);
    List<Task> findTop5ByOrderByIdDesc();
    // 🔥 Dashboard features
    long countByStatus(TaskStatus status);

    long countByAssignedUser(User user);

    // 🔥 Pagination + filtering
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

    // 🔥 Search (title-based)
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Task> searchTasks(String keyword, Pageable pageable);
}