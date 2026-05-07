package edu.touro.mco152.bm;

/**
 * A no-operation implementation of BenchmarkObserver that can be used for tests
 */
public class NoOpBenchmarkObserver implements BenchmarkObserver {

    @Override
    public void initializeDisplay() {
    }

    @Override
    public void showDiskInfo(String diskInfo) {
    }

    @Override
    public void updateProgress(int percentComplete) {
    }

    @Override
    public void publishMark(DiskMark mark) {
    }

    @Override
    public void handleClearCacheRequest() {
    }

    @Override
    public void handleReadError(String message) {
        // no-op: log it at most
        System.err.println("Read error (no-op): " + message);
    }
}