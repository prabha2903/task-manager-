package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // 🔥 MUST HAVE THIS
    List<ActivityLog> findByTaskId(Long taskId);
}