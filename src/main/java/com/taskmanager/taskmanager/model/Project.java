package com.taskmanager.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    // ── Phase 2: assigned team ───────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","members","createdBy"})
    private Team assignedTeam;

    // ── Phase 3: GitHub repository linking ──────────────────────────────────
    /** Full GitHub repo URL e.g. https://github.com/username/repo */
    private String githubRepoUrl;

    /** When the repo was last linked/updated */
    private LocalDateTime githubLinkedAt;
}
