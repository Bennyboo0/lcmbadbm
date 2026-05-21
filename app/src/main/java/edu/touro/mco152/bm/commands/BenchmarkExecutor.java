package edu.touro.mco152.bm.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * An invoker class that stores and executes a collection of benchmark commands.
 * Important Usage Note: This object is intended for a single use cycle.
 * Because commands are not removed from the internal collection upon execution,
 * reusing the same executor instance to run subsequent tasks will cause all previously
 * added commands to re-execute.
 */
public class BenchmarkExecutor {

    private final List<BenchmarkCommand> commands = new ArrayList<>();

    /**
     * Adds a benchmark command to the execution collection.
     */
    public void addCommand(BenchmarkCommand command) {
        commands.add(command);
    }

    /**
     * Executes all registered benchmark commands sequentially.
     * Note: Executed commands remain inside the collection after this method completes.
     * If this executor instance is invoked again, these commands will re-execute.
     */
    public void executeAll() {
        for (BenchmarkCommand command : commands) {
            command.execute();
        }
    }
}