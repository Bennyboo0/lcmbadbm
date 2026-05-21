package edu.touro.mco152.bm;
import edu.touro.mco152.bm.commands.BenchmarkExecutor;
import edu.touro.mco152.bm.persist.DatabasePersistenceObserver;
import edu.touro.mco152.bm.ui.Gui;
import java.util.logging.Level;
import java.util.logging.Logger;
import static edu.touro.mco152.bm.App.*;
import edu.touro.mco152.bm.commands.WriteBM;
import edu.touro.mco152.bm.commands.ReadBM;
import edu.touro.mco152.bm.ui.GuiRunPanelObserver;

/**
 * Contains the core disk benchmark logic, decoupled from any UI or threading framework.
 * Depends only on BenchmarkObserver (for UI callbacks) and BenchmarkRunner
 * (for cancellation checks), both injected via constructor — satisfying DIP.
 */
public class DiskWorker {

    //to run, in terminal: ./gradlew clean build
    //then: ./gradlew run
    private final BenchmarkObserver observer;
    private final BenchmarkRunner runner;  // used only for isCancelled()

    /**
     * @param observer the UI callback handler (Swing or no-op)
     * @param runner   the runner controlling this worker (needed for isCancelled())
     */
    public DiskWorker(BenchmarkObserver observer, BenchmarkRunner runner) {
        this.observer = observer;
        this.runner = runner;
    }

    /**
     * Executes the full benchmark. Call this from whatever thread the runner provides.
     * @return true if completed successfully, false otherwise
     */
    public Boolean run() throws Exception {

        Logger.getLogger(App.class.getName()).log(Level.INFO, "*** New worker thread started ***");
        msg("Running readTest " + App.readTest + "   writeTest " + App.writeTest);
        msg("num files: " + App.numOfMarks + ", num blks: " + App.numOfBlocks
                + ", blk size (kb): " + App.blockSizeKb + ", blockSequence: " + App.blockSequence);

        observer.initializeDisplay();

        if (App.autoReset) {
            App.resetTestData();
            Gui.resetTestData();
        }

        BenchmarkExecutor executor = new BenchmarkExecutor();

        DatabasePersistenceObserver dbObserver = new DatabasePersistenceObserver();
        GuiRunPanelObserver guiObserver = new GuiRunPanelObserver(Gui.runPanel);
        BenchmarkRulesObserver rulesObserver = new BenchmarkRulesObserver();

        if (App.writeTest) {
            WriteBM writeCmd = new WriteBM(
                    runner, observer,
                    App.numOfMarks, App.numOfBlocks, App.blockSizeKb, App.blockSequence, App.multiFile, App.writeSyncEnable
            );

            // Register all three structural observers to the Write command
            writeCmd.registerObserver(dbObserver);
            writeCmd.registerObserver(guiObserver);
            writeCmd.registerObserver(rulesObserver);

            executor.addCommand(writeCmd);
        }

        if (App.readTest) {
            ReadBM readCmd = new ReadBM(
                    runner, observer,
                    App.numOfMarks, App.numOfBlocks, App.blockSizeKb, App.blockSequence, App.multiFile
            );

            // Register all three structural observers to the Read command
            readCmd.registerObserver(dbObserver);
            readCmd.registerObserver(guiObserver);
            readCmd.registerObserver(rulesObserver);

            executor.addCommand(readCmd);
        }

        executor.executeAll();

        App.nextMarkNumber += App.numOfMarks;
        return true;
    }
}