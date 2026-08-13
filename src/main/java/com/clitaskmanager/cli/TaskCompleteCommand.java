package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.Task;
import com.clitaskmanager.domain.service.TaskService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
    name = "complete",
    description = "Marks a task as COMPLETED"
)
public class TaskCompleteCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "ID of the task to complete")
    private long id;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            TaskService service = new TaskService(new SqliteTaskRepository());

            Task completed = service.completeTask(id);
            System.out.println("Task #" + completed.id() + " marked as COMPLETED.");
            System.out.println("Completed at: " + completed.completedAt());
            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
