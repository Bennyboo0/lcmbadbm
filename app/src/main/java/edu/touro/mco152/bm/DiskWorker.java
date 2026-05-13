package edu.touro.mco152.bm;
import edu.touro.mco152.bm.ui.Gui;
import java.util.logging.Level;
import java.util.logging.Logger;
import static edu.touro.mco152.bm.App.*;

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

        int blockSize = blockSizeKb * KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) blockArr[b] = (byte) 0xFF;
        }

        observer.initializeDisplay();

        if (App.autoReset) {
            App.resetTestData();
            Gui.resetTestData();
        }


        if (App.writeTest) {
            WriteBM wbr = new WriteBM();
            wbr.execute(runner, observer);
        }

        if (App.readTest && App.writeTest && !runner.isCancelled()) {
            observer.handleClearCacheRequest();
        }
        if (App.readTest) {
            ReadBM rbm = new ReadBM();
            if(!rbm.execute(runner, observer)){
                return false;
            }
        }

        App.nextMarkNumber += App.numOfMarks;
        return true;
    }
}