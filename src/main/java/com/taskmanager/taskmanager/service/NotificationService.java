package com.taskmanager.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // 🔔 Send to ALL users
    public void notifyAllUsers(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

    // 🔔 Send to specific user (future use)
    public void notifyUser(String email, String message) {
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                message
        );
    }
}