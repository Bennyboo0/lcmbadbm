package edu.touro.mco152.bm.commands;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkExecutor {

    private final List<BenchmarkCommand> commands = new ArrayList<>();

    public void addCommand(BenchmarkCommand command) {
        commands.add(command);
    }

    public void executeAll() {
        for (BenchmarkCommand command : commands) {
            command.execute();
        }
    }
}