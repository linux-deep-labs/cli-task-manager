package com.clitaskmanager.cli;

import com.clitaskmanager.domain.service.TaskService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
    name = "delete",
    description = "Permanently deletes a task"
)
public class TaskDeleteCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "ID of the task to delete")
    private long id;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            TaskService service = new TaskService(new SqliteTaskRepository());

            service.deleteTask(id);
            System.out.println("Task #" + id + " deleted successfully.");
            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
