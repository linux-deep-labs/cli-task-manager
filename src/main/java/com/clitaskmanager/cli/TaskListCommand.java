package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.model.TaskStatus;
import com.clitaskmanager.domain.service.TaskService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "list",
    description = "Lists tasks with optional status and priority filters"
)
public class TaskListCommand implements Callable<Integer> {

    @Option(names = {"--status", "-s"}, description = "Filter by status: PENDING, IN_PROGRESS, COMPLETED")
    private TaskStatus statusFilter;

    @Option(names = {"--priority", "-p"}, description = "Filter by priority: LOW, MEDIUM, HIGH")
    private TaskPriority priorityFilter;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            TaskService service = new TaskService(new SqliteTaskRepository());

            List<Task> tasks = service.listTasks(statusFilter, priorityFilter);

            if (tasks.isEmpty()) {
                System.out.println("No tasks found matching filter criteria.");
                return 0;
            }

            System.out.printf("%-4s %-12s %-8s %-12s %-12s %s%n", "ID", "STATUS", "PRIORITY", "DUE DATE", "FOCUS TIME", "TITLE");
            System.out.println("----------------------------------------------------------------");
            for (Task t : tasks) {
                String dueDateStr = t.dueDate() != null ? t.dueDate().toString() : "-";
                long totalSecs = t.totalFocusSeconds();
                long hours = totalSecs / 3600;
                long minutes = (totalSecs % 3600) / 60;
                long seconds = totalSecs % 60;
                String focusTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                System.out.printf("%-4d %-12s %-8s %-12s %-12s %s%n",
                    t.id(), t.status(), t.priority(), dueDateStr, focusTimeStr, t.title());
            }
            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
