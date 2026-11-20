package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.GitHubLinkRequest;
import com.taskmanager.taskmanager.dto.ProjectSummaryDto;
import com.taskmanager.taskmanager.model.*;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.repository.*;
import com.taskmanager.taskmanager.service.ActivityLogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository  projectRepository;
    private final TaskRepository     taskRepository;
    private final TeamRepository     teamRepository;
    private final UserRepository     userRepository;
    private final ActivityLogService activityLogService;

    public ProjectController(ProjectRepository projectRepository,
                             TaskRepository taskRepository,
                             TeamRepository teamRepository,
                             UserRepository userRepository,
                             ActivityLogService activityLogService) {
        this.projectRepository  = projectRepository;
        this.taskRepository     = taskRepository;
        this.teamRepository     = teamRepository;
        this.userRepository     = userRepository;
        this.activityLogService = activityLogService;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @PostMapping
    public Project createProject(@RequestBody Project project) {
        return projectRepository.save(project);
    }

    // ── LIST ──────────────────────────────────────────────────────────────────
    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // ── DETAIL (with tasks + team + completion %) ─────────────────────────────
    @GetMapping("/{id}")
    public Object getById(@PathVariable Long id) {
        Project    project   = requireProject(id);
        List<Task> tasks     = taskRepository.findByProjectId(id);
        long total           = taskRepository.countByProjectId(id);
        long completed       = taskRepository.countByProjectIdAndStatus(id, TaskStatus.DONE);
        double percent       = total == 0 ? 0 : (completed * 100.0) / total;

        return Map.of(
            "project",              project,
            "tasks",                tasks,
            "totalTasks",           total,
            "completedTasks",       completed,
            "completionPercentage", percent
        );
    }

    // ── SUMMARY (for progress dashboard) ─────────────────────────────────────
    @GetMapping("/{id}/summary")
    public ProjectSummaryDto getSummary(@PathVariable Long id) {
        Project project = requireProject(id);

        long total       = taskRepository.countByProjectId(id);
        long done        = taskRepository.countByProjectIdAndStatus(id, TaskStatus.DONE);
        long backlog     = taskRepository.countByProjectIdAndStatus(id, TaskStatus.BACKLOG);
        long inProgress  = taskRepository.countByProjectIdAndStatus(id, TaskStatus.IN_PROGRESS);
        long review      = taskRepository.countByProjectIdAndStatus(id, TaskStatus.REVIEW);
        long qa          = taskRepository.countByProjectIdAndStatus(id, TaskStatus.QA);
        long blocked     = taskRepository.countByProjectIdAndStatus(id, TaskStatus.BLOCKED);
        double progress  = total == 0 ? 0 : Math.round((done * 100.0) / total);

        Team   team        = project.getAssignedTeam();
        int    teamSize    = team != null && team.getMembers() != null ? team.getMembers().size() : 0;
        String teamName    = team != null ? team.getName() : null;
        String leadName    = (team != null && team.getTeamLead() != null) ? team.getTeamLead().getName() : null;
        String linkedAt    = project.getGithubLinkedAt() != null
                             ? project.getGithubLinkedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                             : null;

        return ProjectSummaryDto.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(total)
                .completedTasks(done)
                .backlogTasks(backlog)
                .inProgressTasks(inProgress)
                .reviewTasks(review)
                .qaTasks(qa)
                .blockedTasks(blocked)
                .progress(progress)
                .teamSize(teamSize)
                .teamName(teamName)
                .teamLeadName(leadName)
                .githubRepoUrl(project.getGithubRepoUrl())
                .githubLinkedAt(linkedAt)
                .build();
    }

    // ── ACTIVITY FEED ─────────────────────────────────────────────────────────
    @GetMapping("/{id}/activities")
    public Page<ActivityLog> getActivities(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        requireProject(id);
        return activityLogService.getLogsByProject(id, page, size);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @PutMapping("/{id}")
    public Project updateProject(@PathVariable Long id, @RequestBody Project updated) {
        Project project = requireProject(id);
        project.setName(updated.getName());
        project.setDescription(updated.getDescription());
        return projectRepository.save(project);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectRepository.delete(requireProject(id));
    }

    // ── ASSIGN TEAM ───────────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @PutMapping("/{projectId}/team/{teamId}")
    public Project assignTeam(@PathVariable Long projectId, @PathVariable Long teamId,
                              Principal principal) {
        Project project = requireProject(projectId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        project.setAssignedTeam(team);
        Project saved = projectRepository.save(project);
        User user = requireUser(principal.getName());
        activityLogService.logProject(user, "PROJECT_TEAM_CHANGED",
                user.getName() + " assigned team '" + team.getName() + "' to project '" + project.getName() + "'",
                saved);
        return saved;
    }

    // ── REMOVE TEAM ───────────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @DeleteMapping("/{projectId}/team")
    public Project removeTeam(@PathVariable Long projectId, Principal principal) {
        Project project = requireProject(projectId);
        project.setAssignedTeam(null);
        return projectRepository.save(project);
    }

    // ── LINK GITHUB REPO ──────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    @PutMapping("/{id}/github")
    public Project linkGitHub(@PathVariable Long id,
                              @Valid @RequestBody GitHubLinkRequest req,
                              Principal principal) {
        Project project = requireProject(id);
        String  url     = req.getRepoUrl();
        // Normalise — ensure https:// prefix
        if (!url.startsWith("http")) url = "https://" + url;

        project.setGithubRepoUrl(url);
        project.setGithubLinkedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);

        User user = requireUser(principal.getName());
        boolean isUpdate = project.getGithubRepoUrl() != null;
        activityLogService.logProject(user,
                isUpdate ? "GITHUB_REPOSITORY_UPDATED" : "GITHUB_REPOSITORY_LINKED",
                user.getName() + " linked GitHub repository: " + url, saved);

        return saved;
    }

    // ── REMOVE GITHUB REPO ────────────────────────────────────────────────────
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_PROJECT_LEAD')")
    @DeleteMapping("/{id}/github")
    public Project removeGitHub(@PathVariable Long id, Principal principal) {
        Project project = requireProject(id);
        String  oldUrl  = project.getGithubRepoUrl();
        project.setGithubRepoUrl(null);
        project.setGithubLinkedAt(null);
        Project saved = projectRepository.save(project);

        User user = requireUser(principal.getName());
        activityLogService.logProject(user, "GITHUB_REPOSITORY_REMOVED",
                user.getName() + " removed GitHub repository: " + (oldUrl != null ? oldUrl : ""), saved);

        return saved;
    }
}
