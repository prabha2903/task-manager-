package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Team;
import com.taskmanager.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);
    boolean existsByName(String name);

    /** Teams where a given user is a member */
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m = :user")
    List<Team> findByMember(@Param("user") User user);

    /** Teams where a given user is the lead */
    List<Team> findByTeamLead(User teamLead);

    /** Teams created by a given user */
    List<Team> findByCreatedBy(User createdBy);
}
