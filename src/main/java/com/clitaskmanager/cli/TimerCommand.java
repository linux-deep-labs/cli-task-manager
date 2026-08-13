package com.clitaskmanager.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "timer",
    description = "Focus timer commands",
    mixinStandardHelpOptions = true,
    subcommands = {
        TimerStartCommand.class,
        TimerStatusCommand.class,
        TimerStopCommand.class
    }
)
public class TimerCommand implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
