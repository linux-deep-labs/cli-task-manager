package com.clitaskmanager.cli;

public class CliErrorHandler {

    public static int handleError(Exception e) {
        System.err.println("Error: " + e.getMessage());
        if (e instanceof IllegalArgumentException) {
            return 1; // User validation or invalid parameter error
        } else if (e instanceof IllegalStateException) {
            return 2; // Invalid state error
        } else {
            return 3; // Database or unexpected execution error
        }
    }
}
