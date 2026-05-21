package edu.touro.mco152.bm.commands;

/**
 * Core interface defining the Command abstraction for disk benchmarking.
 */
public interface BenchmarkCommand {

    /**
     * Invokes the internal benchmark logic encapsulated by the concrete command.
     */
    boolean execute();
}
