package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.CreateTeamRequest;
import com.taskmanager.taskmanager.dto.UpdateTeamRequest;
import com.taskmanager.taskmanager.model.Team;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.TeamRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Team not found: " + id));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return findTeam(id);
    }

    public Team createTeam(CreateTeamRequest req, String creatorEmail) {

        // Validate name
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name is required");
        }

        if (teamRepository.existsByName(req.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A team with name '" + req.getName().trim() + "' already exists");
        }

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));

        // Resolve team lead
        User teamLead = null;
        if (req.getTeamLeadId() != null) {
            teamLead = findUser(req.getTeamLeadId());
        }

        // Resolve members
        List<User> members = new ArrayList<>();
        if (req.getMemberIds() != null) {
            for (Long uid : req.getMemberIds()) {
                members.add(findUser(uid));
            }
        }

        // Auto-include team lead in members if not already
        if (teamLead != null) {
            final User lead = teamLead;
            if (members.stream().noneMatch(m -> m.getId().equals(lead.getId()))) {
                members.add(teamLead);
            }
        }

        // Validate team lead is a member
        if (teamLead != null) {
            final User lead = teamLead;
            boolean leadIsMember = members.stream().anyMatch(m -> m.getId().equals(lead.getId()));
            if (!leadIsMember) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Team Lead must be a member of the team");
            }
        }

        Team team = Team.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .teamLead(teamLead)
                .members(members)
                .createdBy(creator)
                .build();

        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, UpdateTeamRequest req, String editorEmail) {
        Team team = findTeam(id);

        if (req.getName() != null && !req.getName().isBlank()) {
            String newName = req.getName().trim();
            // Allow same name (no change); reject if different team has that name
            if (!newName.equals(team.getName()) && teamRepository.existsByName(newName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A team with name '" + newName + "' already exists");
            }
            team.setName(newName);
        }

        if (req.getDescription() != null) {
            team.setDescription(req.getDescription());
        }

        if (req.getMemberIds() != null) {
            List<User> members = new ArrayList<>();
            for (Long uid : req.getMemberIds()) {
                members.add(findUser(uid));
            }
            team.setMembers(members);
        }

        if (req.getTeamLeadId() != null) {
            User newLead = findUser(req.getTeamLeadId());
            // Lead must be a member
            final List<User> currentMembers = team.getMembers();
            boolean isMember = currentMembers.stream().anyMatch(m -> m.getId().equals(newLead.getId()));
            if (!isMember) {
                // Auto-add lead to members
                currentMembers.add(newLead);
            }
            team.setTeamLead(newLead);
        }

        return teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        Team team = findTeam(id);
        teamRepository.delete(team);
    }

    // ── Member management ─────────────────────────────────────────────────────

    public Team addMember(Long teamId, Long userId) {
        Team team = findTeam(teamId);
        User user = findUser(userId);
        if (team.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member");
        }
        team.getMembers().add(user);
        return teamRepository.save(team);
    }

    public Team removeMember(Long teamId, Long userId) {
        Team team = findTeam(teamId);
        // Cannot remove team lead
        if (team.getTeamLead() != null && team.getTeamLead().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot remove the Team Lead. Change the Team Lead first.");
        }
        team.getMembers().removeIf(m -> m.getId().equals(userId));
        return teamRepository.save(team);
    }

    public Team changeTeamLead(Long teamId, Long newLeadId) {
        Team team = findTeam(teamId);
        User newLead = findUser(newLeadId);
        // Auto-add to members if not already
        if (team.getMembers().stream().noneMatch(m -> m.getId().equals(newLeadId))) {
            team.getMembers().add(newLead);
        }
        team.setTeamLead(newLead);
        return teamRepository.save(team);
    }
}
