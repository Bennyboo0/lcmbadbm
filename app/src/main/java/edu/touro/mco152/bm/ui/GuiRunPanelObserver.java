package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.BenchmarkRunObserver;
import edu.touro.mco152.bm.persist.DiskRun;

/**
 * This is the gui observer side of things. As you suggested, it is in the ui package.
 */
public class GuiRunPanelObserver implements BenchmarkRunObserver {
    private final RunPanel runPanel;

    public GuiRunPanelObserver(RunPanel runPanel) {
        this.runPanel = runPanel;
    }
    @Override
    public void addRun(DiskRun run) {
        runPanel.addRun(run);
    }
}
