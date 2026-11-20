package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Sprint;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.enums.SprintStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

/**
 * Email notifications for TaskFlow.
 * Falls back gracefully when SMTP is not configured — app never crashes.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean        mailEnabled;

    @Value("${spring.mail.username:noreply@taskflow.app}")
    private String fromAddress;

    public EmailService(
            @Value("${spring.mail.host:}") String mailHost,
            org.springframework.beans.factory.ObjectProvider<JavaMailSender> senderProvider) {

        boolean enabled = false;
        JavaMailSender sender = null;
        if (mailHost != null && !mailHost.isBlank()) {
            try {
                sender  = senderProvider.getObject();
                enabled = true;
                log.info("✅ Email service enabled — SMTP host: {}", mailHost);
            } catch (Exception e) {
                log.warn("⚠️  Email service disabled: {}", e.getMessage());
            }
        } else {
            log.info("ℹ️  Email service disabled — MAIL_HOST not set. Email notifications will be skipped.");
        }
        this.mailSender  = sender;
        this.mailEnabled = enabled;
    }

    // ── Task assigned ─────────────────────────────────────────────────────────
    public void sendTaskAssigned(String toEmail, Task task) {
        if (!mailEnabled) return;
        String subject = "Task Assigned: " + task.getTitle();
        String body =
            "<h2>Hello,</h2>" +
            "<p>You have been assigned the task:</p>" +
            "<h3>" + task.getTitle() + "</h3>" +
            "<p><b>Project:</b> " + (task.getProject() != null ? task.getProject().getName() : "N/A") + "</p>" +
            "<p><b>Priority:</b> " + task.getPriority() + "</p>" +
            "<p><b>Status:</b> " + task.getStatus() + "</p>" +
            "<p>Please open TaskFlow to view the task details.</p>" +
            "<br><p>Thanks,<br>TaskFlow</p>";
        send(toEmail, subject, body);
    }

    // ── Task BLOCKED ──────────────────────────────────────────────────────────
    public void sendTaskBlocked(String toEmail, Task task) {
        if (!mailEnabled) return;
        send(toEmail,
            "⚠️ Task Blocked: " + task.getTitle(),
            "<h2>Task Blocked</h2><p>The task <b>" + task.getTitle() +
            "</b> has been moved to BLOCKED status.</p><p>Please review it in TaskFlow.</p>");
    }

    // ── Task completed ────────────────────────────────────────────────────────
    public void sendTaskCompleted(String toEmail, Task task) {
        if (!mailEnabled) return;
        send(toEmail,
            "✅ Task Completed: " + task.getTitle(),
            "<h2>Task Completed</h2><p><b>" + task.getTitle() + "</b> has been marked as DONE. Great work!</p>");
    }

    // ── Comment added ─────────────────────────────────────────────────────────
    public void sendCommentAdded(String toEmail, Task task, String commenterName) {
        if (!mailEnabled) return;
        send(toEmail,
            "💬 New Comment on: " + task.getTitle(),
            "<h2>New Comment</h2><p><b>" + commenterName + "</b> commented on task: <b>" +
            task.getTitle() + "</b></p><p>Open TaskFlow to view the comment.</p>");
    }

    // ── Sprint notification ───────────────────────────────────────────────────
    public void sendSprintNotification(String toEmail, Sprint sprint, SprintStatus status) {
        if (!mailEnabled) return;
        String label = status == SprintStatus.ACTIVE ? "Started" : "Completed";
        String fmt   = sprint.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        send(toEmail,
            "Sprint " + label + ": " + sprint.getName(),
            "<h2>Sprint " + label + "</h2>" +
            "<p>Sprint <b>" + sprint.getName() + "</b> has been " + label.toLowerCase() + ".</p>" +
            "<p><b>Project:</b> " + sprint.getProject().getName() + "</p>" +
            "<p><b>Goal:</b> " + (sprint.getGoal() != null ? sprint.getGoal() : "N/A") + "</p>");
    }

    // ── Core send ─────────────────────────────────────────────────────────────
    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("📧 Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.warn("⚠️  Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
