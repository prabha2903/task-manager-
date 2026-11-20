package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Sprint;
import com.taskmanager.taskmanager.model.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectId(Long projectId);
    List<Sprint> findByProjectIdAndStatus(Long projectId, SprintStatus status);
}
