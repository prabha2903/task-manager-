package com.taskmanager.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.taskmanager.taskmanager.model.enums.TaskPriority;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    private LocalDate dueDate;

    // ✅ ADD THIS
    private LocalDate createdAt;

    // ✅ ADD THIS
    private LocalDate completedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User assignedUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Project project;

    // ✅ AUTO SET CREATED DATE
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
}