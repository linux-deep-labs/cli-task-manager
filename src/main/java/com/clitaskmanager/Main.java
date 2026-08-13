package com.clitaskmanager;

import com.clitaskmanager.cli.TaskCommand;
import com.clitaskmanager.cli.TimerCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "cli-task-manager",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "CLI Task Manager & Focus Timer application",
    subcommands = {
        TaskCommand.class,
        TimerCommand.class
    }
)
public class Main implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
