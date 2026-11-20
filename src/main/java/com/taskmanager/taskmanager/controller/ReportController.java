package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/burndown")
    public List<Map<String, Object>> burndown() {
        return reportService.getBurndown();
    }

    @GetMapping("/task-trend")
    public List<Map<String, Object>> trend() {
        return reportService.getTaskTrend();
    }

    @GetMapping("/user-velocity")
    public List<Map<String, Object>> velocity() {
        return reportService.getUserVelocity();
    }
}