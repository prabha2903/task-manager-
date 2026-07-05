package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // 🔥 Burndown (simplified - active tasks count per day)
    public List<Map<String, Object>> getBurndown() {

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate day = LocalDate.now().minusDays(i);

            long pending = taskRepository.countActiveTasks();

            Map<String, Object> row = new HashMap<>();
            row.put("date", day.toString());
            row.put("remaining", pending);

            result.add(row);
        }

        return result;
    }

    // 🔥 Task creation trend
    public List<Map<String, Object>> getTaskTrend() {

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate day = LocalDate.now().minusDays(i);

            long created = taskRepository.countCreatedBetween(day, day);

            Map<String, Object> row = new HashMap<>();
            row.put("date", day.toString());
            row.put("count", created);

            result.add(row);
        }

        return result;
    }

    // 🔥 User velocity
    public List<Map<String, Object>> getUserVelocity() {

        List<Map<String, Object>> result = new ArrayList<>();

        userRepository.findAll().forEach(user -> {

            long completed = taskRepository.countCompletedByUser(user.getId());

            Map<String, Object> row = new HashMap<>();
            row.put("user", user.getName());
            row.put("completed", completed);

            result.add(row);
        });

        return result;
    }
}