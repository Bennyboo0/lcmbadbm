package edu.touro.mco152.bm;

/**
 * Abstraction for executing and controlling a benchmark run.
 * Decouples DiskWorker's logic from any specific threading mechanism
 * (e.g. SwingWorker, plain Thread), satisfying DIP.
 */
public interface BenchmarkRunner {
    void execute();

    void cancel();

    boolean isCancelled();

    Boolean getLastStatus();
}