package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.TaskPriority;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final TaskRepository taskRepository;

    /**
     * Global full-text search across tasks.
     * Respects existing authentication — only authenticated users can search.
     * Results include task title, description, assignee name, project name.
     *
     * GET /api/search/tasks?q=jwt&status=&priority=&userId=&projectId=&page=0&size=20
     */
    @GetMapping("/tasks")
    public Page<Task> searchTasks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long   userId,
            @RequestParam(required = false) Long   projectId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        TaskStatus   ts = null;
        TaskPriority tp = null;

        if (status != null && !status.isBlank()) {
            try { ts = TaskStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        if (priority != null && !priority.isBlank()) {
            try { tp = TaskPriority.valueOf(priority.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        String keyword = (q != null && !q.isBlank()) ? q.trim() : null;

        return taskRepository.fullTextSearch(
            keyword, ts, tp, userId, projectId,
            PageRequest.of(page, Math.min(size, 50))
        );
    }
}
