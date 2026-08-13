package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.service.TaskService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "info",
    description = "Displays detailed information and focus logs for a task"
)
public class TaskInfoCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "ID of the task")
    private long id;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            TaskService taskService = new TaskService(new SqliteTaskRepository());
            SqliteFocusSessionRepository focusRepo = new SqliteFocusSessionRepository();

            Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task #" + id + " not found"));

            List<FocusSession> sessions = focusRepo.findByTaskId(id);

            System.out.println("Task #" + task.id() + " Details");
            System.out.println("----------------------------------------");
            System.out.println("Title: " + task.title());
            System.out.println("Description: " + (task.description() != null ? task.description() : "-"));
            System.out.println("Status: " + task.status());
            System.out.println("Priority: " + task.priority());
            System.out.println("Due Date: " + (task.dueDate() != null ? task.dueDate() : "-"));
            System.out.println("Created At: " + task.createdAt());
            System.out.println("Completed At: " + (task.completedAt() != null ? task.completedAt() : "-"));
            
            long totalSecs = task.totalFocusSeconds();
            long mins = totalSecs / 60;
            long secs = totalSecs % 60;
            System.out.printf("Total Focus Time: %dm %02ds (%d sessions)%n", mins, secs, sessions.size());

            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
