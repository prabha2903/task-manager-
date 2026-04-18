package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.service.TaskService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ✅ CREATE TASK
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Task createTask(@RequestBody CreateTaskRequest request,
                           Principal principal){
        return taskService.createTask(request, principal.getName());
    }

    // ✅ GET ALL TASKS
    @GetMapping
    public List<Task> getAllTasks(){
        return taskService.getAllTasks();
    }

    // ✅ GET TASK BY ID
    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id){
        return taskService.getTaskById(id);
    }

    // ✅ DELETE TASK
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteTask(@PathVariable Long id,
                           Principal principal){
        taskService.deleteTask(id, principal.getName());
    }

    // ✅ UPDATE STATUS
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_DEVELOPER')")
    public Task updateStatus(@PathVariable Long id,
                             @RequestParam String status,
                             Principal principal){
        return taskService.updateTaskStatus(id, status, principal.getName());
    }

    // 🔥 GET TASKS BY USER
    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId){
        return taskService.getTasksByUser(userId);
    }

    // 🔥 SEARCH + PAGINATION
    @GetMapping("/search")
    public Page<Task> searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        return taskService.searchTasks(keyword, page, size);
    }
    // 🔥 GET RECENT TASKS
    @GetMapping("/recent")
    public List<Task> getRecentTasks() {
        return taskService.getRecentTasks();
    }
}