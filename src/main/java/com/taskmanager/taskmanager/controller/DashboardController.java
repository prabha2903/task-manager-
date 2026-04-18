package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.service.DashboardService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 🔥 Get dashboard data for logged-in user
    @GetMapping
    public ResponseEntity<?> getDashboard(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok(
                dashboardService.getDashboard(principal.getName())
        );
    }
}