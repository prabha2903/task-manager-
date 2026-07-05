package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import com.taskmanager.taskmanager.model.enums.TaskPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    // ✅ Get tasks by assigned user
    List<Task> findByAssignedUser(User user);
    List<Task> findTop5ByOrderByIdDesc();
    // 🔥 Dashboard features
    long countByStatus(TaskStatus status);

    long countByAssignedUser(User user);
    long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
    // 🔥 Pagination + filtering
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

    // 🔥 Search (title-based)
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Task> searchTasks(String keyword, Pageable pageable);
    List<Task> findByDueDateBetweenAndStatusNot(
            LocalDate start,
            LocalDate end,
            TaskStatus status
    );

    @Query("""
SELECT t FROM Task t
WHERE (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
AND (:status IS NULL OR t.status = :status)
AND (:priority IS NULL OR t.priority = :priority)
AND (:userId IS NULL OR t.assignedUser.id = :userId)
AND (:projectId IS NULL OR t.project.id = :projectId)
""")
    Page<Task> searchTasksWithFilters(
            @Param("keyword") String keyword,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("userId") Long userId,
            @Param("projectId") Long projectId, // 🔥 ADD THIS
            Pageable pageable
    );
    // 🔥 ADD THESE AT THE BOTTOM

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status <> 'DONE'")
    long countActiveTasks();

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE t.createdAt BETWEEN :start AND :end
""")
    long countCreatedBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
    SELECT COUNT(t)
    FROM Task t
    WHERE t.assignedUser.id = :userId
    AND t.status = 'DONE'
""")
    long countCompletedByUser(@Param("userId") Long userId);
}
