package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestBody User updatedUser) {

        return userService.updateUser(id, updatedUser);
    }

    @PutMapping("/{id}/password")
    public User changePassword(@PathVariable Long id,
                               @RequestBody Map<String, String> body) {

        return userService.changePassword(
                id,
                body.get("currentPassword"),
                body.get("newPassword")
        );
    }
    @PutMapping("/{id}/role")
    public User changeUserRole(@PathVariable Long id,
                               @RequestBody Map<String, String> body) {

        return userService.changeUserRole(
                id,
                body.get("role")
        );
    }
}