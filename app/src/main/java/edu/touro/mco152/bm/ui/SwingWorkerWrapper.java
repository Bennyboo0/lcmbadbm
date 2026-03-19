package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.DiskMark;

/**
 * This interface exposes the protected SwingWorker methods that SwingBenchmarkObserver
 * needs to call (setProgress), without breaking encapsulation elsewhere. Now aint that something!
 */
public interface SwingWorkerWrapper {

    /**
     * Public-facing wrapper around SwingWorker's protected setProgress().
     * @param percent 0–100
     */
    void setProgressPublic(int percent);
}