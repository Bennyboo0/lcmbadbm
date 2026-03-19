package edu.touro.mco152.bm;
/**
 * This is the UI abstraction
 */
public interface BenchmarkObserver {
    void initializeDisplay();
    void showDiskInfo(String diskInfo);
    void updateProgress(int percentComplete);
    void publishMark(DiskMark mark);
    void handleClearCacheRequest();
}
