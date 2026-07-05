package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.DashboardResponse;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public DashboardResponse getDashboard(String email) {

        // 🔥 Validate email
        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email cannot be null or empty");
        }

        // ✅ Get logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // ✅ Fetch counts safely
        long totalTasks = taskRepository.count();

        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);

        long pendingTasks =
                taskRepository.countByStatus(TaskStatus.BACKLOG)
                        + taskRepository.countByStatus(TaskStatus.IN_PROGRESS)
                        + taskRepository.countByStatus(TaskStatus.REVIEW)
                        + taskRepository.countByStatus(TaskStatus.QA)
                        + taskRepository.countByStatus(TaskStatus.BLOCKED); // 🔥 include IN_PROGRESS

        long userTasks = taskRepository.countByAssignedUser(user);
        long overdueTasks = taskRepository.countByDueDateBeforeAndStatusNot(
                java.time.LocalDate.now(),
                TaskStatus.DONE
        );
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        List<Task> upcomingTasks = taskRepository
                .findByDueDateBetweenAndStatusNot(today, nextWeek, TaskStatus.DONE);
        // ✅ Return response
        return DashboardResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .userTasks(userTasks)
                .overdueTasks(overdueTasks)
                .upcomingTasks(upcomingTasks)
                .build();
    }
}