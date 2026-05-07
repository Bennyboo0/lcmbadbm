package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.*;
import javax.swing.SwingWorker;
import java.util.logging.Logger;

/**
 * Swing-specific implementation
 * Runs DiskWorker inside a SwingWorker so the benchmark executes on a
 * background thread while progress updates arrive safely on the EDT.
 * All Swing threading dependency is isolated here, satisfying DIP and SRP.
 */
public class SwingWorkerRunner implements BenchmarkRunner {

    private final BenchmarkObserver observer;
    private SwingWorker<Boolean, Void> swingWorker;
    private Boolean lastStatus = null;

    /** @param observer the Swing-specific observer to pass to DiskWorker */
    public SwingWorkerRunner(BenchmarkObserver observer) {
        this.observer = observer;
    }

    @Override
    public void execute() {
        swingWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                DiskWorker dw = new DiskWorker(observer, SwingWorkerRunner.this);
                return dw.run();
            }

            @Override
            protected void done() {
                try {
                    lastStatus = get();
                } catch (Exception e) {
                    Logger.getLogger(SwingWorkerRunner.class.getName())
                            .warning("Problem obtaining final status: " + e.getMessage());
                }
                App.state = App.State.IDLE_STATE;
                Gui.mainFrame.adjustSensitivity();
                if (App.autoRemoveData) {
                    Util.deleteDirectory(App.dataDir);
                }
            }
        };

        swingWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int value = (Integer) evt.getNewValue();
                Gui.progressBar.setValue(value);
                long kbProcessed = value * App.targetTxSizeKb() / 100;
                Gui.progressBar.setString(kbProcessed + " / " + App.targetTxSizeKb());
            }
        });

        swingWorker.execute();
    }

    /**
     * Updates the Swing progress bar. Called by SwingBenchmarkObserver
     * via the BenchmarkRunner reference.
     */
    public void setProgressPublic(int percent) {
        if (swingWorker != null) {
            swingWorker.firePropertyChange("progress", -1, percent);
        }
    }

    @Override
    public void cancel() {
        if (swingWorker != null) swingWorker.cancel(true);
    }

    @Override
    public boolean isCancelled() {
        return swingWorker != null && swingWorker.isCancelled();
    }

    @Override
    public Boolean getLastStatus() {
        return lastStatus;
    }
}