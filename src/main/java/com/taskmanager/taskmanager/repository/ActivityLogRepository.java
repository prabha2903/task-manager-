package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByTaskIdOrderByTimestampDesc(Long taskId);

    /** Legacy method — keep for backward compat */
    default List<ActivityLog> findByTaskId(Long taskId) {
        return findByTaskIdOrderByTimestampDesc(taskId);
    }

    /** All activities for a project (task events + direct project events) */
    @Query("""
        SELECT a FROM ActivityLog a
        WHERE a.project.id = :projectId
           OR (a.task IS NOT NULL AND a.task.project.id = :projectId)
        ORDER BY a.timestamp DESC
        """)
    Page<ActivityLog> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);
}
