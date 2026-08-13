package com.clitaskmanager.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "task",
    description = "Task management commands",
    mixinStandardHelpOptions = true,
    subcommands = {
        TaskAddCommand.class,
        TaskListCommand.class,
        TaskUpdateCommand.class,
        TaskCompleteCommand.class,
        TaskDeleteCommand.class,
        TaskInfoCommand.class
    }
)
public class TaskCommand implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
