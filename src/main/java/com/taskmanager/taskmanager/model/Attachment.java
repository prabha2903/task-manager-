package com.taskmanager.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Original name shown to the user */
    private String originalFileName;

    /** UUID-based safe name stored on disk */
    private String storedFileName;

    /** MIME type e.g. application/pdf */
    private String fileType;

    /** Size in bytes */
    private Long fileSize;

    /** Relative path under the upload directory */
    private String storagePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    @JsonIgnoreProperties({"description","assignedUser","project","createdBy"})
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploaded_by_id")
    @JsonIgnoreProperties({"password","role"})
    private User uploadedBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}
