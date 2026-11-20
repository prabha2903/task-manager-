package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.enums.RoleType;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.repository.RoleRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RoleRepository    roleRepository;
    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    // ─── existing methods ────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(updatedUser.getName());
        return userRepository.save(user);
    }

    public User changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User changeUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByName(RoleType.valueOf(roleName.toUpperCase()))
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRole(role);
        return userRepository.save(user);
    }

    // ─── NEW: advanced filter ────────────────────────────────────────────────

    /**
     * Filter users by keyword (name/email), role, project, task status,
     * and assignment status. All params are optional (null = no filter).
     */
    public List<User> filterUsers(String keyword,
                                  String role,
                                  Long   projectId,
                                  String taskStatus,
                                  String assignmentStatus) {

        RoleType roleType = null;
        if (role != null && !role.isBlank()) {
            try { roleType = RoleType.valueOf(role.toUpperCase()); }
            catch (IllegalArgumentException ignored) { /* bad value → skip */ }
        }

        TaskStatus status = null;
        if (taskStatus != null && !taskStatus.isBlank()) {
            try { status = TaskStatus.valueOf(taskStatus.toUpperCase()); }
            catch (IllegalArgumentException ignored) { }
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String as = (assignmentStatus != null && !assignmentStatus.isBlank()) ? assignmentStatus.toUpperCase() : null;

        return userRepository.filterUsers(kw, roleType, projectId, status, as);
    }
}
