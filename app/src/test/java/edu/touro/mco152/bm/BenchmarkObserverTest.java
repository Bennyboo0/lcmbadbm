package edu.touro.mco152.bm;


import edu.touro.mco152.bm.App;
import edu.touro.mco152.bm.DiskMark;
import edu.touro.mco152.bm.commands.ReadBM;
import edu.touro.mco152.bm.commands.WriteBM;
import edu.touro.mco152.bm.persist.DiskRun;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These are Verification Tests.
 * It ensures that benchmark subjects correctly implement the Observer Pattern
 */
public class BenchmarkObserverTest {

    private boolean observerWasCalled;

    @BeforeEach
    public void setUp() {
        observerWasCalled = false;

        App.numOfMarks = 1;
        App.numOfBlocks = 1;
        App.blockSizeKb = 4;
        App.blockSequence = DiskRun.BlockSequence.SEQUENTIAL;
        App.nextMarkNumber = 1;
        App.multiFile = false;
        App.writeSyncEnable = false;

        App.locationDir = new File(System.getProperty("user.dir"));
        App.dataDir = new File(App.locationDir, "jDiskMarkData");

        if (!App.dataDir.exists()) {
            App.dataDir.mkdirs();
        }

        App.testFile = new File(App.dataDir, "testdata.jdm");

        App.readTest = true;
        App.writeTest = true;
    }

    @Test
    public void testWriteBMEndOfBenchmarkNotification() {
        //this constructs the Write subject
        WriteBM writeCmd = new WriteBM(
                new NoOpBenchmarkRunner(),
                new NoOpBenchmarkObserver(),
                1, 1, 4,
                DiskRun.BlockSequence.SEQUENTIAL,
                false, false
        );

        writeCmd.registerObserver(new BenchmarkRunObserver() {
            @Override
            public void addRun(DiskRun run) {
                observerWasCalled = true;
            }
        });

        writeCmd.execute();
    }

    @Test
    public void testReadBMEndOfBenchmarkNotification() {
        try {
            if (!App.testFile.exists()) {
                App.testFile.createNewFile();
            }
        } catch (Exception ignored) {}

        //this construct the Read subject
        ReadBM readCmd = new ReadBM(
                new NoOpBenchmarkRunner(),
                new NoOpBenchmarkObserver(),
                1, 1, 4,
                DiskRun.BlockSequence.SEQUENTIAL,
                false
        );

        readCmd.registerObserver(new BenchmarkRunObserver() {
            @Override
            public void addRun(DiskRun run) {
                observerWasCalled = true;
            }
        });

        readCmd.execute();

        if (App.testFile.exists()) {
            App.testFile.delete();
        }
    }

    /**
     * this fulfills your Requirement: Asserts that our registered observer was invoked
     * immediately after the execution of the current test method lifecycle.
     */
    @AfterEach
    public void verifyObserversInvoked() {
        assertTrue(observerWasCalled,
                "Observer Pattern Failure: The benchmark command failed to notify its registered observers!");
    }


    private static class NoOpBenchmarkRunner implements BenchmarkRunner {
        @Override public void execute() {}
        @Override public void cancel() {}
        @Override public boolean isCancelled() { return false; }
        @Override public Boolean getLastStatus() {return null;}
    }

    private static class NoOpBenchmarkObserver implements BenchmarkObserver {
        @Override public void initializeDisplay() {}
        @Override public void showDiskInfo(String diskInfo) {}
        @Override public void updateProgress(int percentComplete) {}
        @Override public void publishMark(DiskMark mark) {}
        @Override public void handleClearCacheRequest() {}
        @Override public void handleReadError(String message) {}
        @Override public void addRun(DiskRun run) {}
    }
}