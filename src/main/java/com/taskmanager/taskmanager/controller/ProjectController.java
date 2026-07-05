package com.taskmanager.taskmanager.controller;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.repository.ProjectRepository;

import org.springframework.security.access.prepost.PreAuthorize; // ✅ IMPORTANT IMPORT
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    public ProjectController(ProjectRepository projectRepository,
                             TaskRepository taskRepository){
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }
    // ✅ Create Project (ADMIN only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public Project createProject(@RequestBody Project project){
        return projectRepository.save(project);
    }

    // ✅ Get all projects
    // ✅ Get all projects
    @GetMapping
    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    // ✅ 🔥 ADD THIS METHOD
    @GetMapping("/{id}")
    public Object getById(@PathVariable Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(id);

        long total = taskRepository.countByProjectId(id);
        long completed = taskRepository.countByProjectIdAndStatus(id, TaskStatus.DONE);

        double percent = total == 0 ? 0 : (completed * 100.0) / total;

        return Map.of(
                "project", project,
                "tasks", tasks,
                "totalTasks", total,
                "completedTasks", completed,
                "completionPercentage", percent
        );
    }

    // ✅ UPDATE PROJECT
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Project updateProject(@PathVariable Long id, @RequestBody Project updatedProject) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());

        return projectRepository.save(project);
    }

    // ✅ DELETE PROJECT
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }
}