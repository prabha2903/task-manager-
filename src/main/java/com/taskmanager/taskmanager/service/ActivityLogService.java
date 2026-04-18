package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.ActivityLog;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    // 🔥 Save activity log
    public void log(User user, String action, Task task) {

        if (user == null || task == null) {
            throw new RuntimeException("User or Task cannot be null");
        }

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .task(task)
                .timestamp(LocalDateTime.now())
                .build();

        activityLogRepository.save(log);

        System.out.println("🔥 LOG SAVED: " + action);
    }

    // 🔥 ADD THIS BACK ✅
    public List<ActivityLog> getLogsByTask(Long taskId) {

        if (taskId == null) {
            throw new RuntimeException("Task ID cannot be null");
        }

        return activityLogRepository.findByTaskId(taskId);
    }
}