package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.BenchmarkObserver;
import edu.touro.mco152.bm.DiskMark;

import javax.swing.JOptionPane;

/**
 * Swing-specific implementation of BenchmarkObserve.
 */
public class SwingBenchmarkObserver implements BenchmarkObserver {

    private SwingWorkerWrapper worker;

    //I can now actually get rid of this constructor.
    public SwingBenchmarkObserver() {

    }

    /** this method allows the worker to be set after construction, resolving the circular dependency. */
    public void setWorker(SwingWorkerWrapper worker) {
        this.worker = worker;
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
        worker.setProgressPublic(percentComplete);
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
                        independantly by doing a cold reboot after the WRITE""", //I got this message from AI, I think it looks pretty official, no?
                "Clear Disk Cache Now", JOptionPane.PLAIN_MESSAGE);
    }
}