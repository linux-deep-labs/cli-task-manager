package com.clitaskmanager.cli;

import com.clitaskmanager.domain.model.FocusSession;
import com.clitaskmanager.domain.service.FocusTimerService;
import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@Command(
    name = "start",
    description = "Starts a focus timer session"
)
public class TimerStartCommand implements Callable<Integer> {

    @Option(names = {"--task-id", "-t"}, description = "ID of the task to associate with")
    private Long taskId;

    @Option(names = {"--duration", "-d"}, description = "Duration in minutes (Default: 25)")
    private int durationMinutes = 25;

    @Override
    public Integer call() {
        try {
            DatabaseConnectionManager.initializeDatabase();
            FocusTimerService timerService = new FocusTimerService(new SqliteFocusSessionRepository(), new SqliteTaskRepository());

            FocusSession session = timerService.startTimer(taskId, durationMinutes);

            System.out.println("Focus timer started!");
            System.out.println("Duration: " + session.plannedDurationMinutes() + " minutes");
            if (session.taskId() != null) {
                System.out.println("Linked Task: #" + session.taskId());
            }
            Instant targetEnd = session.startedAt().plusSeconds(session.plannedDurationMinutes() * 60L);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
            System.out.println("Target End Time: " + formatter.format(targetEnd));

            return 0;
        } catch (Exception e) {
            return CliErrorHandler.handleError(e);
        }
    }
}
