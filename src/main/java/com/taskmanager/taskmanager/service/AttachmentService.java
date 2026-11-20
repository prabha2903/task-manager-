package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Attachment;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.AttachmentRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository       taskRepository;
    private final UserRepository       userRepository;
    private final ActivityLogService   activityLogService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB

    /** Dangerous file extensions — reject these */
    private static final Set<String> BLOCKED_EXT = Set.of(
        "exe","bat","cmd","sh","ps1","vbs","js","jar","msi","dll","com","scr","pif"
    );

    // ── Upload ────────────────────────────────────────────────────────────────

    public Attachment upload(Long taskId, MultipartFile file, String email) {

        // Validate size
        if (file.getSize() > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File is too large. Maximum allowed size is 10 MB.");
        }

        // Validate original name
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
        }

        // Strip path traversal
        originalName = Paths.get(originalName).getFileName().toString();

        // Block dangerous extensions
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot + 1).toLowerCase();
        if (BLOCKED_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File type ." + ext + " is not allowed.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User uploader = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Generate safe stored file name
        String storedName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);

        // Store file
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path dest = dir.resolve(storedName).normalize();

            // Double-check no path traversal
            if (!dest.startsWith(dir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path.");
            }

            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file: " + e.getMessage());
        }

        Attachment attachment = Attachment.builder()
                .originalFileName(originalName)
                .storedFileName(storedName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(uploadDir + "/" + storedName)
                .task(task)
                .uploadedBy(uploader)
                .build();

        Attachment saved = attachmentRepository.save(attachment);

        // Log activity
        activityLogService.log(uploader, "ATTACHMENT_ADDED",
                uploader.getName() + " uploaded " + originalName + " to task: " + task.getTitle(), task);

        return saved;
    }

    // ── List by task ──────────────────────────────────────────────────────────

    public List<Attachment> getByTask(Long taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    // ── Download ──────────────────────────────────────────────────────────────

    public Attachment findById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));
    }

    public Path resolveStoredFile(Attachment a) {
        Path dir  = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path file = dir.resolve(a.getStoredFileName()).normalize();
        if (!file.startsWith(dir)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }
        if (!Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on server.");
        }
        return file;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(Long id, String email, boolean isAdminOrManager) {
        Attachment a = findById(id);
        boolean isOwner = a.getUploadedBy().getEmail().equals(email);
        if (!isOwner && !isAdminOrManager) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own attachments.");
        }
        try {
            Path path = Paths.get(uploadDir).toAbsolutePath().normalize()
                    .resolve(a.getStoredFileName()).normalize();
            Files.deleteIfExists(path);
        } catch (IOException ignored) { }
        attachmentRepository.delete(a);
    }
}
