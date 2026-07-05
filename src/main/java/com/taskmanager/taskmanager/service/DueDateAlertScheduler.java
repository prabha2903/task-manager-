package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DueDateAlertScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    // ⏰ Runs every day at 8 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void checkDueTasks() {

        LocalDate today = LocalDate.now();

        List<Task> tasks = taskRepository.findAll();

        for (Task task : tasks) {

            if (task.getDueDate() != null &&
                    task.getStatus() != TaskStatus.DONE &&
                    task.getDueDate().equals(today)) {

                if (task.getAssignedUser() != null) {

                    notificationService.notifyUser(
                            task.getAssignedUser().getEmail(),
                            "⏰ Task due today: " + task.getTitle()
                    );
                }
            }
        }
    }
}