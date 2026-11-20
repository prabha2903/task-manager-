package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.CreateSprintRequest;
import com.taskmanager.taskmanager.model.Sprint;
import com.taskmanager.taskmanager.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("/api/projects/{projectId}/sprints")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Sprint createSprint(@PathVariable Long projectId,
                               @Valid @RequestBody CreateSprintRequest req,
                               Principal principal) {
        return sprintService.create(projectId, req, principal.getName());
    }

    @GetMapping("/api/projects/{projectId}/sprints")
    public List<Sprint> getSprintsByProject(@PathVariable Long projectId) {
        return sprintService.getByProject(projectId);
    }

    @GetMapping("/api/sprints/{id}")
    public Sprint getSprint(@PathVariable Long id) {
        return sprintService.getById(id);
    }

    @GetMapping("/api/sprints/{id}/summary")
    public Map<String, Object> getSprintSummary(@PathVariable Long id) {
        return sprintService.getSummary(id);
    }

    @PutMapping("/api/sprints/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Sprint updateSprint(@PathVariable Long id,
                               @RequestBody CreateSprintRequest req,
                               Principal principal) {
        return sprintService.update(id, req, principal.getName());
    }

    @PutMapping("/api/sprints/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Sprint changeStatus(@PathVariable Long id,
                               @RequestBody Map<String, String> body,
                               Principal principal) {
        return sprintService.changeStatus(id, body.get("status"), principal.getName());
    }

    @DeleteMapping("/api/sprints/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSprint(@PathVariable Long id) {
        sprintService.delete(id);
    }

    @PostMapping("/api/sprints/{sprintId}/tasks/{taskId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Sprint addTask(@PathVariable Long sprintId, @PathVariable Long taskId) {
        return sprintService.addTask(sprintId, taskId);
    }

    @DeleteMapping("/api/sprints/{sprintId}/tasks/{taskId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    public Sprint removeTask(@PathVariable Long sprintId, @PathVariable Long taskId) {
        return sprintService.removeTask(sprintId, taskId);
    }
}
