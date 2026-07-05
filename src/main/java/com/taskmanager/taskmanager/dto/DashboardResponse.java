
package com.taskmanager.taskmanager.dto;
import java.util.List;
import com.taskmanager.taskmanager.model.Task;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
public class DashboardResponse {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long userTasks;
    private long overdueTasks;
    private List<Task> upcomingTasks;
}