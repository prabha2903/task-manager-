package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogService activityLogService;

    // ✅ CREATE TASK
    public Task createTask(CreateTaskRequest request, String email) {

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }

        // 🔥 Logged-in user
        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        User assignedUser = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assigned user not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setAssignedUser(assignedUser);
        task.setProject(project);

        Task savedTask = taskRepository.save(task);

        // 🔥 Correct logging
        activityLogService.log(loggedInUser, "Task created", savedTask);

        return savedTask;
    }

    // ✅ GET ALL TASKS
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // ✅ GET TASK BY ID
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"));
    }

    // ✅ DELETE TASK
    public void deleteTask(Long id, String email) {

        Task task = getTaskById(id);

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // 🔥 Log BEFORE delete
        activityLogService.log(loggedInUser, "Task deleted", task);

        taskRepository.delete(task);
    }

    // ✅ UPDATE TASK STATUS
    public Task updateTaskStatus(Long taskId, String status, String email) {

        Task task = getTaskById(taskId);

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        TaskStatus taskStatus;

        try {
            taskStatus = TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status value");
        }

        task.setStatus(taskStatus);

        Task updatedTask = taskRepository.save(task);

        activityLogService.log(
                loggedInUser,
                "Status updated to " + status,
                updatedTask
        );

        return updatedTask;
    }

    // ✅ GET TASKS BY USER
    public List<Task> getTasksByUser(Long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return taskRepository.findByAssignedUser(user);
    }

    // 🔥 SEARCH + PAGINATION
    public Page<Task> searchTasks(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return taskRepository.searchTasks(keyword, pageable);
    }
    public List<Task> getRecentTasks() {
        return taskRepository.findTop5ByOrderByIdDesc();
    }
}