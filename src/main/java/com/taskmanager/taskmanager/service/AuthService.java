package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.*;
import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.enums.RoleType;
import com.taskmanager.taskmanager.repository.RoleRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.security.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists"
            );
        }

        RoleType roleType;

         String requested = request.getRole() == null ? "" : request.getRole().trim().toUpperCase();

        if (userRepository.count() == 0) {
            roleType = RoleType.ADMIN;            // the very first account becomes Admin
        } else if ("PROJECT_LEAD".equals(requested)) {
            roleType = RoleType.PROJECT_LEAD;     // allowed at sign-up
        } else {
            roleType = RoleType.DEVELOPER;        // everything else is Developer
        }

        Role role = roleRepository
                .findByName(roleType)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid role"
                ));
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)   // ✅ correct for ManyToOne
                .build();

        userRepository.save(user);

        return "User Registered Successfully";
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid password"
            );
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() != null && user.getRole().getName() != null
                        ? user.getRole().getName().name()
                        : null
        );
    }
}