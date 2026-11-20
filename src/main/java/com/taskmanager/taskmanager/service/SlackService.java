package com.taskmanager.taskmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

/**
 * Slack webhook notifications.
 * Webhook URL is NEVER returned to the frontend — server-side only.
 * Failures do NOT break TaskFlow operations.
 */
@Service
@Slf4j
public class SlackService {

    @Value("${slack.webhook.url:}")
    private String webhookUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient   httpClient   = HttpClient.newHttpClient();

    public boolean isEnabled() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    /**
     * Send a simple Slack message. Never throws — failure is logged only.
     */
    public void send(String text) {
        if (!isEnabled()) {
            log.debug("Slack not configured — skipping notification");
            return;
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of("text", text));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                log.info("✅ Slack notification sent");
            } else {
                log.warn("⚠️  Slack returned HTTP {}: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            log.warn("⚠️  Slack notification failed: {}", e.getMessage());
        }
    }

    public void sendTaskEvent(String event, String taskTitle, String projectName, String userName) {
        String emoji = switch (event) {
            case "ASSIGNED"  -> "🔔";
            case "COMPLETED" -> "✅";
            case "BLOCKED"   -> "⛔";
            default          -> "📌";
        };
        send(emoji + " *TaskFlow Update*\n" +
             "*Project:* " + projectName + "\n" +
             "*Task:* " + taskTitle + "\n" +
             "*Event:* " + event + "\n" +
             "*By:* " + userName);
    }

    public void sendSprintEvent(String event, String sprintName, String projectName) {
        String emoji = event.contains("STARTED") ? "🚀" : "🏁";
        send(emoji + " *Sprint " + event + "*\n" +
             "*Sprint:* " + sprintName + "\n" +
             "*Project:* " + projectName);
    }

    /** Test connection — returns true if Slack responds OK */
    public boolean testConnection() {
        if (!isEnabled()) return false;
        try {
            send("🧪 TaskFlow test message — Slack integration is working!");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
