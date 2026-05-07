package edu.touro.mco152.bm;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Non-Swing implementation of BenchmarkRunner.
 * Runs DiskWorker on a plain Java thread — no Swing dependency.
 * Used by JUnit tests and any non-GUI client.
 */
public class PlainThreadRunner implements BenchmarkRunner {

    private final BenchmarkObserver observer;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile Boolean lastStatus = null;
    private Thread workerThread;

    public PlainThreadRunner(BenchmarkObserver observer) {
        this.observer = observer;
    }

    @Override
    public void execute() {
        workerThread = new Thread(() -> {
            try {
                DiskWorker dw = new DiskWorker(observer, this);
                lastStatus = dw.run();
            } catch (Exception e) {
                lastStatus = false;
            }
        });
        workerThread.start();
    }

    public void waitForCompletion() throws InterruptedException {
        if (workerThread != null) workerThread.join();
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public Boolean getLastStatus() {
        return lastStatus;
    }
}