package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class DiskWorkerTest {

    @BeforeEach
    void setup() {
        setupDefaultAsPerProperties();
    }

    /**
     * Bruteforce setup of static classes/fields to allow DiskWorker to run.
     * @author lcmcohen
     */
    private static void setupDefaultAsPerProperties() {
        Gui.mainFrame = new MainFrame();
        App.p = new Properties();
        App.loadConfig();

        Gui.progressBar = Gui.mainFrame.getProgressBar();

        System.setProperty("derby.system.home", App.APP_CACHE_DIR);

        if (App.locationDir == null) {
            App.locationDir = new File(System.getProperty("user.home"));
        }

        App.dataDir = new File(App.locationDir.getAbsolutePath()
                + File.separator + App.DATADIRNAME);

        if (App.dataDir.exists()) {
            if (App.dataDir.delete()) {
                App.msg("removed existing data dir");
            } else {
                App.msg("unable to remove existing data dir");
            }
        } else {
            App.dataDir.mkdirs();
        }
    }

    @Test
    void testBenchmarkRunsWithoutSwing() throws Exception {

        // Track progress updates to verify the engine is reporting them
        AtomicInteger lastProgressSeen = new AtomicInteger(-1);

        // Use an anonymous subclass of NoOpBenchmarkObserver to capture progress
        BenchmarkObserver testObserver = new NoOpBenchmarkObserver() {
            @Override
            public void updateProgress(int percentComplete) {
                lastProgressSeen.set(percentComplete);
            }
        };

        // Configure a small, fast benchmark
        App.writeTest = true;
        App.readTest = false;
        App.numOfMarks = 2;
        App.numOfBlocks = 16;
        App.blockSizeKb = 128;
        App.blockSequence = edu.touro.mco152.bm.persist.DiskRun.BlockSequence.SEQUENTIAL;
        App.multiFile = false;
        App.autoReset = true;

        // Create and run the worker with no Swing involved
        DiskWorker worker = new DiskWorker(testObserver);
        worker.execute();

        // Wait for completion (SwingWorker runs on a background thread)
        Boolean result = worker.get();

        // Assert it completed successfully
        assertTrue(result, "Benchmark should complete successfully");

        // Assert that progress was reported and reached a valid value
        assertTrue(lastProgressSeen.get() > 0,
                "Progress should have been reported during execution");
    }
}