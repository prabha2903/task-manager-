package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.ActivityLog;
import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    // ── EXISTING: task-level log (backward compatible) ───────────────────────
    public void log(User user, String action, String detail, Task task) {
        if (user == null) throw new RuntimeException("User cannot be null");
        // task CAN be null for project-level events — remove old null check
        save(user, action, detail, task, task != null ? task.getProject() : null);
    }

    // ── NEW: project-level log (no task) ─────────────────────────────────────
    public void logProject(User user, String action, String detail, Project project) {
        if (user == null || project == null) throw new RuntimeException("User and Project required");
        save(user, action, detail, null, project);
    }

    private void save(User user, String action, String detail, Task task, Project project) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .detail(detail)
                .task(task)
                .project(project)
                .timestamp(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<ActivityLog> getLogsByTask(Long taskId) {
        return activityLogRepository.findByTaskIdOrderByTimestampDesc(taskId);
    }

    public Page<ActivityLog> getLogsByProject(Long projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByProjectId(projectId, pageable);
    }
}
