package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.service.FocusTimerService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "stop",
    description = "Stops the active focus session and logs recorded focus time"
)
public class TimerStopCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            FocusTimerService timerService = new FocusTimerService(new SqliteFocusSessionRepository(), new SqliteTaskRepository());

            FocusSession stopped = timerService.stopTimer();

            long totalSecs = stopped.actualDurationSeconds();
            long minutes = totalSecs / 60;
            long seconds = totalSecs % 60;

            System.out.println("Focus session stopped and saved to SQLite database.");
            System.out.printf("Actual Focus Duration: %d minutes %d seconds.%n", minutes, seconds);
            if (stopped.taskId() != null) {
                System.out.println("Recorded under Task #" + stopped.taskId());
            }

            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
