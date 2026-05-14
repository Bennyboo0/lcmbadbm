package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commands.BenchmarkCommand;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.BeforeAll;
import edu.touro.mco152.bm.commands.WriteBM;
import edu.touro.mco152.bm.commands.ReadBM;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class CommandPatternTest {

    static final int NUM_MARKS = 25;
    static final int NUM_BLOCKS = 128;
    static final int BLOCK_SIZE_KB = 2048;
    static final DiskRun.BlockSequence SEQUENCE = DiskRun.BlockSequence.SEQUENTIAL;

    @BeforeAll
    static void initApp() {
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
        }
        App.dataDir.mkdirs();


        App.multiFile = false;
        App.autoReset = true;
    }

    @Test
    void testWriteCommand() {
        BenchmarkObserver observer = new NoOpBenchmarkObserver();
        BenchmarkRunner runner = new BenchmarkRunner() {
            @Override
            public void execute() {

            }

            @Override
            public void cancel() {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public Boolean getLastStatus() {
                return null;
            }
        };
        BenchmarkCommand write = new WriteBM(runner, observer, NUM_MARKS, NUM_BLOCKS, BLOCK_SIZE_KB, SEQUENCE,
                false, false);
        assertTrue(write.execute());
    }

    @Test
    void testReadCommand() {
        BenchmarkObserver observer = new NoOpBenchmarkObserver();
        BenchmarkRunner runner = new BenchmarkRunner() {
            @Override
            public void execute() {}

            @Override
            public void cancel() {}

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public Boolean getLastStatus() {
                return null;
            }
        };

        //the thing is that we have to write something b4 we can read it. so here we do that.
        BenchmarkCommand write = new WriteBM(runner, observer, NUM_MARKS, NUM_BLOCKS, BLOCK_SIZE_KB, SEQUENCE,
                false, false);

        write.execute();
        App.nextMarkNumber = 1;


        BenchmarkCommand read = new ReadBM(
                runner, observer, NUM_MARKS, NUM_BLOCKS, BLOCK_SIZE_KB, SEQUENCE, false);
        assertTrue(read.execute());
    }
}