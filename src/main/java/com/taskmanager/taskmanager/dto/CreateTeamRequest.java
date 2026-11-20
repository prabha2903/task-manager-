package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    private String name;

    private String description;

    /** User ID of the designated Team Lead */
    private Long teamLeadId;

    /** List of user IDs to add as members */
    private List<Long> memberIds;

    // Getters & Setters
    public String getName()             { return name; }
    public void   setName(String v)     { name = v; }
    public String getDescription()                { return description; }
    public void   setDescription(String v)        { description = v; }
    public Long   getTeamLeadId()                 { return teamLeadId; }
    public void   setTeamLeadId(Long v)           { teamLeadId = v; }
    public List<Long> getMemberIds()              { return memberIds; }
    public void       setMemberIds(List<Long> v)  { memberIds = v; }
}
