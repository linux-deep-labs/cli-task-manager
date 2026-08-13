package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.service.FocusTimerService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(
    name = "status",
    description = "Displays the current timer state"
)
public class TimerStatusCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            FocusTimerService timerService = new FocusTimerService(new SqliteFocusSessionRepository(), new SqliteTaskRepository());

            Optional<FocusSession> activeOpt = timerService.getActiveTimer();
            if (activeOpt.isEmpty()) {
                System.out.println("Timer Status: IDLE (No active focus session)");
                return 0;
            }

            FocusSession active = activeOpt.get();
            long elapsedSeconds = Duration.between(active.startedAt(), Instant.now()).getSeconds();
            long plannedSeconds = active.plannedDurationMinutes() * 60L;
            long remainingSeconds = Math.max(0, plannedSeconds - elapsedSeconds);

            System.out.println("Timer Status: RUNNING");
            if (active.taskId() != null) {
                System.out.println("Linked Task: #" + active.taskId());
            }
            System.out.printf("Elapsed: %dm %02ds / %dm 00s%n", elapsedSeconds / 60, elapsedSeconds % 60, active.plannedDurationMinutes());
            System.out.printf("Remaining: %dm %02ds%n", remainingSeconds / 60, remainingSeconds % 60);

            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
