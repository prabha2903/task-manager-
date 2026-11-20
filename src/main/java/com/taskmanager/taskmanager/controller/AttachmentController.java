package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.Attachment;
import com.taskmanager.taskmanager.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    // ── Upload: POST /api/tasks/{taskId}/attachments ──────────────────────────
    @PostMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<Attachment> upload(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        Attachment saved = attachmentService.upload(taskId, file, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── List: GET /api/tasks/{taskId}/attachments ─────────────────────────────
    @GetMapping("/api/tasks/{taskId}/attachments")
    public List<Attachment> list(@PathVariable Long taskId) {
        return attachmentService.getByTask(taskId);
    }

    // ── Download: GET /api/attachments/{id}/download ──────────────────────────
    @GetMapping("/api/attachments/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment a    = attachmentService.findById(id);
        Path       path = attachmentService.resolveStoredFile(a);
        try {
            Resource resource = new UrlResource(path.toUri());
            String   cd       = ContentDisposition.attachment()
                                    .filename(a.getOriginalFileName()).build().toString();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd)
                    .contentType(MediaType.parseMediaType(
                            a.getFileType() != null ? a.getFileType() : "application/octet-stream"))
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Delete: DELETE /api/attachments/{id} ──────────────────────────────────
    @DeleteMapping("/api/attachments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        // isAdminOrManager resolved via SecurityContext in service via role check
        // Pass false here; service has full logic
        attachmentService.delete(id, principal.getName(), false);
        return ResponseEntity.noContent().build();
    }

    // ── Admin/Manager delete (bypasses owner check) ───────────────────────────
    @DeleteMapping("/api/attachments/{id}/force")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Void> forceDelete(@PathVariable Long id, Principal principal) {
        attachmentService.delete(id, principal.getName(), true);
        return ResponseEntity.noContent().build();
    }
}
