package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.CreateSprintRequest;
import com.taskmanager.taskmanager.model.*;
import com.taskmanager.taskmanager.model.enums.SprintStatus;
import com.taskmanager.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository     sprintRepository;
    private final ProjectRepository    projectRepository;
    private final TaskRepository       taskRepository;
    private final UserRepository       userRepository;
    private final ActivityLogService   activityLogService;
    private final EmailService         emailService;

    private Sprint findSprint(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));
    }

    // ── Create ────────────────────────────────────────────────────────────────
    public Sprint create(Long projectId, CreateSprintRequest req, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Sprint sprint = Sprint.builder()
                .name(req.getName().trim())
                .goal(req.getGoal())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(SprintStatus.PLANNED)
                .project(project)
                .createdBy(creator)
                .build();

        Sprint saved = sprintRepository.save(sprint);
        activityLogService.logProject(creator, "SPRINT_CREATED",
                creator.getName() + " created sprint '" + saved.getName() + "'", project);
        return saved;
    }

    // ── List by project ───────────────────────────────────────────────────────
    public List<Sprint> getByProject(Long projectId) {
        return sprintRepository.findByProjectId(projectId);
    }

    // ── Get single ────────────────────────────────────────────────────────────
    public Sprint getById(Long id) { return findSprint(id); }

    // ── Update ────────────────────────────────────────────────────────────────
    public Sprint update(Long id, CreateSprintRequest req, String email) {
        Sprint sprint = findSprint(id);
        if (req.getName() != null && !req.getName().isBlank()) sprint.setName(req.getName());
        if (req.getGoal() != null) sprint.setGoal(req.getGoal());
        if (req.getStartDate() != null) sprint.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) sprint.setEndDate(req.getEndDate());
        if (sprint.getEndDate().isBefore(sprint.getStartDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        return sprintRepository.save(sprint);
    }

    // ── Change status ─────────────────────────────────────────────────────────
    public Sprint changeStatus(Long id, String statusStr, String email) {
        Sprint sprint = findSprint(id);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SprintStatus newStatus;
        try { newStatus = SprintStatus.valueOf(statusStr.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sprint status");
        }

        SprintStatus old = sprint.getStatus();
        sprint.setStatus(newStatus);
        Sprint saved = sprintRepository.save(sprint);

        String action = newStatus == SprintStatus.ACTIVE ? "SPRINT_STARTED" : "SPRINT_" + newStatus;
        activityLogService.logProject(user, action,
                user.getName() + " changed sprint '" + sprint.getName() + "' to " + newStatus, sprint.getProject());

        // Email team if sprint starts or completes
        if (newStatus == SprintStatus.ACTIVE || newStatus == SprintStatus.COMPLETED) {
            try {
                if (sprint.getProject().getAssignedTeam() != null) {
                    sprint.getProject().getAssignedTeam().getMembers().forEach(m ->
                        emailService.sendSprintNotification(m.getEmail(), sprint, newStatus)
                    );
                }
            } catch (Exception ignored) { /* email failure must not break the operation */ }
        }

        return saved;
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    public void delete(Long id) {
        sprintRepository.delete(findSprint(id));
    }

    // ── Add task ──────────────────────────────────────────────────────────────
    public Sprint addTask(Long sprintId, Long taskId) {
        Sprint sprint = findSprint(sprintId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        boolean alreadyIn = sprint.getTasks().stream().anyMatch(t -> t.getId().equals(taskId));
        if (alreadyIn) throw new ResponseStatusException(HttpStatus.CONFLICT, "Task already in sprint");

        sprint.getTasks().add(task);
        return sprintRepository.save(sprint);
    }

    // ── Remove task ───────────────────────────────────────────────────────────
    public Sprint removeTask(Long sprintId, Long taskId) {
        Sprint sprint = findSprint(sprintId);
        sprint.getTasks().removeIf(t -> t.getId().equals(taskId));
        return sprintRepository.save(sprint);
    }

    // ── Sprint dashboard summary ──────────────────────────────────────────────
    public Map<String, Object> getSummary(Long sprintId) {
        Sprint sprint = findSprint(sprintId);
        List<Task> tasks = sprint.getTasks();
        long total    = tasks.size();
        long done     = tasks.stream().filter(t -> t.getStatus().name().equals("DONE")).count();
        long blocked  = tasks.stream().filter(t -> t.getStatus().name().equals("BLOCKED")).count();
        long inProg   = tasks.stream().filter(t -> t.getStatus().name().equals("IN_PROGRESS")).count();
        long backlog  = tasks.stream().filter(t -> t.getStatus().name().equals("BACKLOG")).count();
        double pct    = total == 0 ? 0 : Math.round((done * 100.0) / total);

        return Map.of(
            "sprint",       sprint,
            "totalTasks",   total,
            "completedTasks", done,
            "blockedTasks", blocked,
            "inProgressTasks", inProg,
            "backlogTasks", backlog,
            "progress",     pct
        );
    }
}
