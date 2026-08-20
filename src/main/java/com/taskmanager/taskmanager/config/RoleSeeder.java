package com.taskmanager.taskmanager.config;

import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.enums.RoleType;
import com.taskmanager.taskmanager.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleType type : RoleType.values()) {
            roleRepository.findByName(type).orElseGet(() ->
                    roleRepository.save(
                            Role.builder()
                                    .name(type)
                                    .build()
                    )
            );
        }
    }
}
