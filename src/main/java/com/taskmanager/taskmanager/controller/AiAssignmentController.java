package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.AiAssignmentService;
import com.taskmanager.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssignmentController {

    private final AiAssignmentService aiService;
    private final TaskRepository      taskRepository;
    private final UserRepository      userRepository;
    private final ProjectRepository   projectRepository;
    private final TaskService         taskService;

    /**
     * Suggest assignments for all unassigned/backlog tasks in a project.
     * Uses the project's assigned team members.
     * MANAGER or PROJECT_LEAD only.
     */
    @PostMapping("/suggest/{projectId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public List<Map<String, Object>> suggestAssignments(@PathVariable Long projectId) {
        var project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId).stream()
            .filter(t -> t.getStatus() != com.taskmanager.taskmanager.model.enums.TaskStatus.DONE)
            .toList();

        List<User> members;
        if (project.getAssignedTeam() != null && project.getAssignedTeam().getMembers() != null) {
            members = project.getAssignedTeam().getMembers();
        } else {
            members = userRepository.findAll();
        }

        return aiService.suggestAssignments(tasks, members);
    }

    /**
     * Approve a single AI suggestion — ACTUALLY assigns the task.
     * This is the human-in-the-loop approval step.
     */
    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Task approveAssignment(@RequestBody Map<String, Long> body, Principal principal) {
        Long taskId = body.get("taskId");
        Long userId = body.get("userId");
        if (taskId == null || userId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId and userId are required");

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        task.setAssignedUser(user);
        return taskRepository.save(task);
    }

    /** Check if AI is available */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("enabled", aiService.isEnabled(),
                      "provider", aiService.isEnabled() ? "Gemini" : "Not configured");
    }
}
