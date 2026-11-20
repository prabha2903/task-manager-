package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.enums.TaskPriority;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // ── Basic finders ─────────────────────────────────────────────────────────
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignedUser(User user);
    List<Task> findTop5ByOrderByIdDesc();

    // ── Counts ────────────────────────────────────────────────────────────────
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    long countByStatus(TaskStatus status);
    long countByAssignedUser(User user);
    long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status <> 'DONE'")
    long countActiveTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = 'DONE'")
    long countCompletedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdAt BETWEEN :start AND :end")
    long countCreatedBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // ── Date helpers ──────────────────────────────────────────────────────────
    List<Task> findByDueDateBetweenAndStatusNot(LocalDate start, LocalDate end, TaskStatus status);

    // ── Paged queries ─────────────────────────────────────────────────────────
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

    // ── Legacy title-only search ──────────────────────────────────────────────
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Task> searchTasks(@Param("keyword") String keyword, Pageable pageable);

    // ── Advanced filter (Phase 1) ─────────────────────────────────────────────
    @Query("""
        SELECT t FROM Task t
        WHERE (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status   IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:userId   IS NULL OR t.assignedUser.id = :userId)
          AND (:projectId IS NULL OR t.project.id = :projectId)
        """)
    Page<Task> searchTasksWithFilters(
            @Param("keyword")   String keyword,
            @Param("status")    TaskStatus status,
            @Param("priority")  TaskPriority priority,
            @Param("userId")    Long userId,
            @Param("projectId") Long projectId,
            Pageable pageable
    );

    // ── Phase 4: Full-text search across title, description, assignee, project ─
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN t.assignedUser u
        LEFT JOIN t.project p
        WHERE
          (:q IS NULL OR :q = ''
            OR LOWER(t.title)        LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(t.description)  LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(u.name)         LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.name)         LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:status    IS NULL OR t.status           = :status)
          AND (:priority  IS NULL OR t.priority         = :priority)
          AND (:userId    IS NULL OR t.assignedUser.id  = :userId)
          AND (:projectId IS NULL OR t.project.id       = :projectId)
        ORDER BY t.id DESC
        """)
    Page<Task> fullTextSearch(
            @Param("q")         String q,
            @Param("status")    TaskStatus status,
            @Param("priority")  TaskPriority priority,
            @Param("userId")    Long userId,
            @Param("projectId") Long projectId,
            Pageable pageable
    );
}
