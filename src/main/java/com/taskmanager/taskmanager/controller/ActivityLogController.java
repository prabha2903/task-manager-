package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.ActivityLog;
import com.taskmanager.taskmanager.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // 🔥 GET ACTIVITY LOGS BY TASK
    @GetMapping("/{taskId}")
    @PreAuthorize("permitAll()")
    public List<ActivityLog> getLogs(@PathVariable Long taskId) {
        return activityLogService.getLogsByTask(taskId);
    }
}