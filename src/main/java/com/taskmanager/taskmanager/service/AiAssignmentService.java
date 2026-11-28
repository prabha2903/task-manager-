package com.taskmanager.taskmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.*;
import java.util.*;

/**
 * AI-assisted task assignment suggestions using Gemini API.
 * AI NEVER auto-assigns — all suggestions require human approval.
 * API key is stored server-side only — NEVER exposed to frontend.
 */
@Service
@Slf4j
public class AiAssignmentService {

    @Value("${AI_API_KEY:}")
    private String apiKey;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Generate task assignment suggestions.
     * Returns a list of {taskId, taskTitle, suggestedUserId, suggestedUserName,
     * reason}
     * The caller (Manager/ProjectLead) must explicitly approve each suggestion.
     */
    public List<Map<String, Object>> suggestAssignments(List<Task> tasks, List<User> teamMembers) {
        if (!isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI service is not configured. Please set AI_API_KEY environment variable.");
        }
        if (tasks.isEmpty() || teamMembers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Need at least one task and one team member to generate suggestions.");
        }

        String prompt = buildPrompt(tasks, teamMembers);

        try {
            String responseText = callGemini(prompt);
            return parseResponse(responseText, tasks, teamMembers);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI assignment suggestion failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI service temporarily unavailable. Please try again or assign manually.");
        }
    }

    private String buildPrompt(List<Task> tasks, List<User> members) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "You are a project manager assistant. Suggest which team member should be assigned to each task.\n\n");
        sb.append("TEAM MEMBERS:\n");
        members.forEach(m -> sb.append("- ID:").append(m.getId())
                .append(" Name:").append(m.getName())
                .append(" Role:").append(m.getRole() != null ? m.getRole().getName() : "DEVELOPER")
                .append("\n"));
        sb.append("\nTASKS:\n");
        tasks.forEach(t -> sb.append("- ID:").append(t.getId())
                .append(" Title:").append(t.getTitle())
                .append(" Priority:").append(t.getPriority())
                .append(" Status:").append(t.getStatus())
                .append("\n"));
        sb.append("\nRespond ONLY with a JSON array. Example:\n");
        sb.append(
                "[{\"taskId\":1,\"userId\":2,\"reason\":\"Best suited based on role\"},{\"taskId\":3,\"userId\":4,\"reason\":\"Available capacity\"}]\n");
        sb.append("Only include tasks that should be reassigned. Do not include tasks that are DONE.");
        return sb.toString();
    }

    private String callGemini(String prompt) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))))));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            log.warn("Gemini API returned {}: {}", resp.statusCode(), resp.body());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI API error: " + resp.statusCode());
        }

        JsonNode root = objectMapper.readTree(resp.body());
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }

    private List<Map<String, Object>> parseResponse(String text, List<Task> tasks, List<User> members) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // Extract JSON array from response (Gemini sometimes adds markdown)
            int start = text.indexOf('[');
            int end = text.lastIndexOf(']');
            if (start < 0 || end < 0)
                throw new RuntimeException("No JSON array in response");
            String json = text.substring(start, end + 1);

            JsonNode arr = objectMapper.readTree(json);
            Map<Long, Task> taskMap = new HashMap<>();
            Map<Long, User> memberMap = new HashMap<>();
            tasks.forEach(t -> taskMap.put(t.getId(), t));
            members.forEach(m -> memberMap.put(m.getId(), m));

            for (JsonNode item : arr) {
                Long taskId = item.path("taskId").asLong();
                Long userId = item.path("userId").asLong();
                String reason = item.path("reason").asText("AI suggestion");

                Task t = taskMap.get(taskId);
                User u = memberMap.get(userId);

                if (t != null && u != null) {
                    result.add(Map.of(
                            "taskId", t.getId(),
                            "taskTitle", t.getTitle(),
                            "suggestedUserId", u.getId(),
                            "suggestedUserName", u.getName(),
                            "reason", reason));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {} — {}", e.getMessage(), text);
            // Return empty list rather than crashing
        }
        return result;
    }
}
