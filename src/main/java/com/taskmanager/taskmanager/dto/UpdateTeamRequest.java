package com.taskmanager.taskmanager.dto;

import java.util.List;

public class UpdateTeamRequest {

    private String name;
    private String description;
    private Long   teamLeadId;
    private List<Long> memberIds;

    public String getName()                       { return name; }
    public void   setName(String v)               { name = v; }
    public String getDescription()                { return description; }
    public void   setDescription(String v)        { description = v; }
    public Long   getTeamLeadId()                 { return teamLeadId; }
    public void   setTeamLeadId(Long v)           { teamLeadId = v; }
    public List<Long> getMemberIds()              { return memberIds; }
    public void       setMemberIds(List<Long> v)  { memberIds = v; }
}
