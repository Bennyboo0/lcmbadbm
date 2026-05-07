package edu.touro.mco152.bm;

/**
 * Abstraction for all UI callbacks made during a benchmark run.
 * Implementations may be Swing-based, no-op (for testing), or any other UI framework,
 * satisfying DIP and SRP.
 */
public interface BenchmarkObserver {
    void initializeDisplay();

    void showDiskInfo(String diskInfo);

    void updateProgress(int percentComplete);

    void publishMark(DiskMark mark);

    void handleClearCacheRequest();

    void handleReadError(String message);
}