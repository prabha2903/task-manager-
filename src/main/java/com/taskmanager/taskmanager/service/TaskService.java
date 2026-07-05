package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.TaskPriority;
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
import com.taskmanager.taskmanager.dto.UpdateTaskRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

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

        User assignedUser;

        if (request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Assigned user not found"));
        } else {
            assignedUser = loggedInUser; // fallback to creator
        }

        Project project = null;

        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Project not found"));
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        // 🔥 ADD THIS CHECK HERE
        if (request.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status cannot be null"
            );
        }

        task.setStatus(request.getStatus());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setAssignedUser(assignedUser);
        task.setProject(project);

        Task savedTask = taskRepository.save(task);
        notificationService.notifyUser(
                assignedUser.getEmail(),
                "🆕 You got a new task: " + savedTask.getTitle()
        );
        // 🔥 Correct logging
        activityLogService.log(
                loggedInUser,
                "TASK_CREATED",
                null,
                savedTask
        );
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

        notificationService.notifyAllUsers(
                "🗑 Task deleted: " + task.getTitle()
        );
        // 🔥 Log BEFORE delete
        activityLogService.log(
                loggedInUser,
                "TASK_DELETED",
                null,
                task
        );

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
        notificationService.notifyAllUsers(
                "🔄 Task updated: " + updatedTask.getTitle() +
                        " → " + updatedTask.getStatus()
        );
        activityLogService.log(
                loggedInUser,
                "STATUS_CHANGED",
                status,
                updatedTask
        );

        return updatedTask;
    }
    public Task updateTask(Long id, UpdateTaskRequest request, String email) {

        Task task = getTaskById(id);

        User loggedInUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (request.getTitle() != null) {
            if (request.getTitle().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Title cannot be empty");
            }
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null)
            task.setDescription(request.getDescription());

        if (request.getPriority() != null)
            task.setPriority(request.getPriority());

        if (request.getStatus() != null)
            task.setStatus(request.getStatus());

        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());

        if (request.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Assigned user not found"));
            task.setAssignedUser(assignedUser);
        }

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Project not found"));
            task.setProject(project);
        }

        Task updatedTask = taskRepository.save(task);

        notificationService.notifyAllUsers(
                "✏️ Task updated by " + loggedInUser.getName() + ": " + updatedTask.getTitle()
        );

        activityLogService.log(
                loggedInUser,
                "TASK_UPDATED",
                null,
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
    public Page<Task> searchTasks(String keyword,
                                  TaskStatus status,
                                  TaskPriority priority,
                                  Long userId,
                                  Long projectId,
                                  int page,
                                  int size) {

        Pageable pageable = PageRequest.of(page, size);

        return taskRepository.searchTasksWithFilters(
                keyword,
                status,
                priority,
                userId,
                projectId,
                pageable
        );
    }
    public List<Task> getRecentTasks() {
        return taskRepository.findTop5ByOrderByIdDesc();
    }
}