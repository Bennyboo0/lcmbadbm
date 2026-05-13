package edu.touro.mco152.bm;
import edu.touro.mco152.bm.commands.BenchmarkExecutor;
import edu.touro.mco152.bm.ui.Gui;
import java.util.logging.Level;
import java.util.logging.Logger;
import static edu.touro.mco152.bm.App.*;
import edu.touro.mco152.bm.commands.WriteBM;
import edu.touro.mco152.bm.commands.ReadBM;

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

        if (App.writeTest) {
            executor.addCommand(new WriteBM(
                    runner, observer,
                    App.numOfMarks, App.numOfBlocks, App.blockSizeKb, App.blockSequence
            ));
        }

        if (App.readTest) {
            executor.addCommand(new ReadBM(
                    runner, observer,
                    App.numOfMarks, App.numOfBlocks, App.blockSizeKb, App.blockSequence
            ));
        }

        executor.executeAll();

        App.nextMarkNumber += App.numOfMarks;
        return true;
    }
}