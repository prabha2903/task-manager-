package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.CreateTeamRequest;
import com.taskmanager.taskmanager.dto.UpdateTeamRequest;
import com.taskmanager.taskmanager.model.Team;
import com.taskmanager.taskmanager.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // ── GET all teams — any authenticated user ────────────────────────────────
    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    // ── GET single team ───────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public Team getTeam(@PathVariable Long id) {
        return teamService.getTeamById(id);
    }

    // ── CREATE team — ADMIN or MANAGER only ───────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@Valid @RequestBody CreateTeamRequest req,
                           Principal principal) {
        return teamService.createTeam(req, principal.getName());
    }

    // ── UPDATE team — ADMIN or MANAGER only ───────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public Team updateTeam(@PathVariable Long id,
                           @RequestBody UpdateTeamRequest req,
                           Principal principal) {
        return teamService.updateTeam(id, req, principal.getName());
    }

    // ── DELETE team — ADMIN only ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

    // ── ADD member — ADMIN or MANAGER ─────────────────────────────────────────
    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public Team addMember(@PathVariable Long id,
                          @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return teamService.addMember(id, userId);
    }

    // ── REMOVE member — ADMIN or MANAGER ──────────────────────────────────────
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public Team removeMember(@PathVariable Long id,
                             @PathVariable Long userId) {
        return teamService.removeMember(id, userId);
    }

    // ── CHANGE team lead — ADMIN or MANAGER ───────────────────────────────────
    @PutMapping("/{id}/lead")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public Team changeTeamLead(@PathVariable Long id,
                               @RequestBody Map<String, Long> body) {
        Long newLeadId = body.get("userId");
        if (newLeadId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return teamService.changeTeamLead(id, newLeadId);
    }
}
