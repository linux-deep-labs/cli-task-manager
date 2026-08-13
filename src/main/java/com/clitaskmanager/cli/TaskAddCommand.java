package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.Task;
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
    name = "add",
    description = "Creates a new task with status PENDING"
)
public class TaskAddCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Title of the task")
    private String title;

    @Option(names = {"--description", "-d"}, description = "Optional description of the task")
    private String description;

    @Option(names = {"--priority", "-p"}, description = "Priority: LOW, MEDIUM, HIGH (Default: MEDIUM)")
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Option(names = {"--due"}, description = "Due date in YYYY-MM-DD format")
    private String dueDateStr;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            TaskService service = new TaskService(new SqliteTaskRepository());

            LocalDate dueDate = dueDateStr != null ? LocalDate.parse(dueDateStr) : null;
            Task created = service.createTask(title, description, priority, dueDate);

            System.out.println("Task #" + created.id() + " created successfully.");
            System.out.println("Title: " + created.title());
            System.out.println("Priority: " + created.priority());
            System.out.println("Status: " + created.status());
            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
