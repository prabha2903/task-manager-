package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateSprintRequest {
    @NotBlank(message = "Sprint name is required")
    private String name;

    private String goal;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    public String getName()                   { return name; }
    public void   setName(String v)           { name = v; }
    public String getGoal()                   { return goal; }
    public void   setGoal(String v)           { goal = v; }
    public LocalDate getStartDate()           { return startDate; }
    public void      setStartDate(LocalDate v){ startDate = v; }
    public LocalDate getEndDate()             { return endDate; }
    public void      setEndDate(LocalDate v)  { endDate = v; }
}
