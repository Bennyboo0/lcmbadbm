package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.BenchmarkObserver;
import edu.touro.mco152.bm.DiskMark;
import javax.swing.JOptionPane;

/**
 * Swing-specific implementation of BenchmarkObserver.
 * All Swing UI interactions during a benchmark run are isolated here, satisfying SRP and DIP.
 */
public class SwingBenchmarkObserver implements BenchmarkObserver {

    private SwingWorkerRunner runner;

    /** Sets the runner after construction to allow progress updates via SwingWorker. */
    public void setRunner(SwingWorkerRunner runner) {
        this.runner = runner;
    }

    @Override
    public void initializeDisplay() {
        Gui.updateLegend();
    }

    @Override
    public void showDiskInfo(String diskInfo) {
        Gui.chartPanel.getChart().getTitle().setVisible(true);
        Gui.chartPanel.getChart().getTitle().setText(diskInfo);
    }

    @Override
    public void updateProgress(int percentComplete) {
        if (runner != null) runner.setProgressPublic(percentComplete);
    }

    @Override
    public void publishMark(DiskMark mark) {
        if (mark.getType() == DiskMark.MarkType.WRITE) {
            Gui.addWriteMark(mark);
        } else {
            Gui.addReadMark(mark);
        }
    }

    @Override
    public void handleClearCacheRequest() {
        JOptionPane.showMessageDialog(Gui.mainFrame,
                """
                For valid READ measurements please clear the disk cache by
                using the included RAMMap.exe or flushmem.exe utilities.
                Removable drives can be disconnected and reconnected.
                For system drives use the WRITE and READ operations\s
                independently by doing a cold reboot after the WRITE""",
                "Clear Disk Cache Now", JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void handleReadError(String message) {
        JOptionPane.showMessageDialog(Gui.mainFrame, message,
                "Unable to READ", JOptionPane.ERROR_MESSAGE);
    }
}