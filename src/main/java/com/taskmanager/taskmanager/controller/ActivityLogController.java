package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.ActivityLog;
import com.taskmanager.taskmanager.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // ── Existing: task-level activity ─────────────────────────────────────────
    @GetMapping("/api/tasks/{taskId}/activity")
    public List<ActivityLog> getByTask(@PathVariable Long taskId) {
        return activityLogService.getLogsByTask(taskId);
    }

    // ── New alias (some frontends use this path) ───────────────────────────────
    @GetMapping("/api/activity/{taskId}")
    public List<ActivityLog> getByTaskAlt(@PathVariable Long taskId) {
        return activityLogService.getLogsByTask(taskId);
    }
}
