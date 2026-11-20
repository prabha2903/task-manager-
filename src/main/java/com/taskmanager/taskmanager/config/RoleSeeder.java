package com.taskmanager.taskmanager.config;

import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.enums.RoleType;
import com.taskmanager.taskmanager.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void seedRoles() {
        for (RoleType type : RoleType.values()) {
            roleRepository.findByName(type).orElseGet(() -> {
                Role role = new Role();
                role.setName(type);
                return roleRepository.save(role);
            });
        }
    }
}
