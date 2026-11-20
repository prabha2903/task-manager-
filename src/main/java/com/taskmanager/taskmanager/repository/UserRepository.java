package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.model.enums.RoleType;
import com.taskmanager.taskmanager.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ── Advanced filter for Team Members page ─────────────────────────────────
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN u.role r
        WHERE
          (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.name)  LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:roleType IS NULL OR r.name = :roleType)
          AND (:projectId IS NULL OR EXISTS (
                SELECT t FROM Task t
                WHERE t.assignedUser = u
                  AND t.project.id = :projectId))
          AND (:taskStatus IS NULL OR EXISTS (
                SELECT t FROM Task t
                WHERE t.assignedUser = u
                  AND t.status = :taskStatus))
          AND (:assignmentStatus IS NULL
            OR (:assignmentStatus = 'ASSIGNED'
                AND EXISTS  (SELECT t FROM Task t WHERE t.assignedUser = u))
            OR (:assignmentStatus = 'UNASSIGNED'
                AND NOT EXISTS (SELECT t FROM Task t WHERE t.assignedUser = u)))
        ORDER BY u.name
        """)
    List<User> filterUsers(
            @Param("keyword")          String keyword,
            @Param("roleType")         RoleType roleType,
            @Param("projectId")        Long projectId,
            @Param("taskStatus")       TaskStatus taskStatus,
            @Param("assignmentStatus") String assignmentStatus
    );
}
