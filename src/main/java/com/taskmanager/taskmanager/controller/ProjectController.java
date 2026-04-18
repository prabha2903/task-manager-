package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.repository.ProjectRepository;

import org.springframework.security.access.prepost.PreAuthorize; // ✅ IMPORTANT IMPORT
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    // ✅ Create Project (ADMIN only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public Project createProject(@RequestBody Project project){
        return projectRepository.save(project);
    }

    // ✅ Get all projects
    @GetMapping
    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }
}