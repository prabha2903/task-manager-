package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/slack")
@RequiredArgsConstructor
public class SlackController {

    private final SlackService slackService;

    /** Status — NEVER exposes the webhook URL */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
            "enabled", slackService.isEnabled(),
            "message", slackService.isEnabled()
                ? "Slack integration is active"
                : "Set SLACK_WEBHOOK_URL environment variable to enable Slack notifications"
        );
    }

    /** Send a test message to verify the integration */
    @PostMapping("/test")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public Map<String, Object> test() {
        boolean ok = slackService.testConnection();
        return Map.of("success", ok,
                      "message", ok ? "Test message sent to Slack!" : "Slack not configured or connection failed");
    }
}
