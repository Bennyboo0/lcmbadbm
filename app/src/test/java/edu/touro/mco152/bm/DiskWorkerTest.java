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

        AtomicInteger lastProgressSeen = new AtomicInteger(-1);

        BenchmarkObserver testObserver = new NoOpBenchmarkObserver() {
            @Override
            public void updateProgress(int percentComplete) {
                lastProgressSeen.set(percentComplete);
            }
        };

        App.writeTest = true;
        App.readTest = false;
        App.numOfMarks = 2;
        App.numOfBlocks = 16;
        App.blockSizeKb = 128;
        App.blockSequence = edu.touro.mco152.bm.persist.DiskRun.BlockSequence.SEQUENTIAL;
        App.multiFile = false;
        App.autoReset = true;

        // Use PlainThreadRunner instead of DiskWorker directly
        PlainThreadRunner runner = new PlainThreadRunner(testObserver);
        runner.execute();
        runner.waitForCompletion();  // blocks until the plain thread finishes

        assertTrue(runner.getLastStatus(), "Benchmark should complete successfully");

        assertTrue(lastProgressSeen.get() > 0,
                "Progress should have been reported during execution");
    }
}