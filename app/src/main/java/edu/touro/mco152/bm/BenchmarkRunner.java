package edu.touro.mco152.bm;

public interface BenchmarkRunner {
    void execute();
    boolean isCancelled();
    Boolean getLastStatus();

}
