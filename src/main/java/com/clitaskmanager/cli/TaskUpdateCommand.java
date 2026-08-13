package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.TaskPriority;
import com.clitaskmanager.domain.service.TaskService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.LocalDate;
import java.util.concurrent.Callable;

@Command(
    name = "update",
    description = "Updates metadata for an existing task"
)
public class TaskUpdateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "ID of the task to update")
    private long id;

    @Option(names = {"--title", "-t"}, description = "New title of the task")
    private String newTitle;

    @Option(names = {"--description", "-d"}, description = "New description of the task")
    private String newDescription;

    @Option(names = {"--priority", "-p"}, description = "New priority: LOW, MEDIUM, HIGH")
    private TaskPriority newPriority;

    @Option(names = {"--due"}, description = "New due date in YYYY-MM-DD format")
    private String newDueDateStr;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            TaskService service = new TaskService(new SqliteTaskRepository());

            LocalDate newDueDate = newDueDateStr != null ? LocalDate.parse(newDueDateStr) : null;
            service.updateTask(id, newTitle, newDescription, newPriority, newDueDate);

            System.out.println("Task #" + id + " updated successfully.");
            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
