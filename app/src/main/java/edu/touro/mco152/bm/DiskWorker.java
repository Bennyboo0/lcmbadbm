package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import edu.touro.mco152.bm.ui.Gui;
import jakarta.persistence.EntityManager;

import javax.swing.*;
import java.io.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.*;
import static edu.touro.mco152.bm.DiskMark.MarkType.*;

/**
 * Contains the core disk benchmark logic, decoupled from any UI or threading framework.
 * Depends only on BenchmarkObserver (for UI callbacks) and BenchmarkRunner
 * (for cancellation checks), both injected via constructor — satisfying DIP.
 */
public class DiskWorker {

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

        int wUnitsComplete = 0, rUnitsComplete = 0, unitsComplete;
        int wUnitsTotal = App.writeTest ? numOfBlocks * numOfMarks : 0;
        int rUnitsTotal = App.readTest ? numOfBlocks * numOfMarks : 0;
        int unitsTotal = wUnitsTotal + rUnitsTotal;
        float percentComplete;

        int blockSize = blockSizeKb * KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) blockArr[b] = (byte) 0xFF;
        }

        DiskMark wMark, rMark;
        observer.initializeDisplay();

        if (App.autoReset) {
            App.resetTestData();
            Gui.resetTestData();
        }

        int startFileNum = App.nextMarkNumber;

        if (App.writeTest) {
            DiskRun run = new DiskRun(DiskRun.IOMode.WRITE, App.blockSequence);
            run.setNumMarks(App.numOfMarks);
            run.setNumBlocks(App.numOfBlocks);
            run.setBlockSize(App.blockSizeKb);
            run.setTxSize(App.targetTxSizeKb());
            run.setDiskInfo(Util.getDiskInfo(dataDir));

            msg("disk info: (" + run.getDiskInfo() + ")");
            observer.showDiskInfo(run.getDiskInfo());

            if (!App.multiFile) {
                testFile = new File(dataDir.getAbsolutePath() + File.separator + "testdata.jdm");
            }

            for (int m = startFileNum; m < startFileNum + App.numOfMarks && !runner.isCancelled(); m++) {

                if (App.multiFile) {
                    testFile = new File(dataDir.getAbsolutePath()
                            + File.separator + "testdata" + m + ".jdm");
                }
                wMark = new DiskMark(WRITE);
                wMark.setMarkNum(m);
                long startTime = System.nanoTime();
                long totalBytesWrittenInMark = 0;

                String mode = App.writeSyncEnable ? "rwd" : "rw";

                try (RandomAccessFile rAccFile = new RandomAccessFile(testFile, mode)) {
                    for (int b = 0; b < numOfBlocks; b++) {
                        if (App.blockSequence == DiskRun.BlockSequence.RANDOM) {
                            rAccFile.seek((long) Util.randInt(0, numOfBlocks - 1) * blockSize);
                        } else {
                            rAccFile.seek((long) b * blockSize);
                        }
                        rAccFile.write(blockArr, 0, blockSize);
                        totalBytesWrittenInMark += blockSize;
                        wUnitsComplete++;
                        unitsComplete = rUnitsComplete + wUnitsComplete;
                        percentComplete = (float) unitsComplete / (float) unitsTotal * 100f;
                        observer.updateProgress((int) percentComplete);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

                long endTime = System.nanoTime();
                double sec = (double)(endTime - startTime) / 1_000_000_000.0;
                double mbWritten = (double) totalBytesWrittenInMark / (double) MEGABYTE;
                wMark.setBwMbSec(mbWritten / sec);
                msg("m:" + m + " write IO is " + wMark.getBwMbSecAsString() + " MB/s");
                App.updateMetrics(wMark);
                observer.publishMark(wMark);

                run.setRunMax(wMark.getCumMax());
                run.setRunMin(wMark.getCumMin());
                run.setRunAvg(wMark.getCumAvg());
                run.setEndTime(new Date());
            }

            EntityManager em = EM.getEntityManager();
            em.getTransaction().begin();
            em.persist(run);
            em.getTransaction().commit();
            Gui.runPanel.addRun(run);
        }

        if (App.readTest && App.writeTest && !runner.isCancelled()) {
            observer.handleClearCacheRequest();
        }

        if (App.readTest) {
            DiskRun run = new DiskRun(DiskRun.IOMode.READ, App.blockSequence);
            run.setNumMarks(App.numOfMarks);
            run.setNumBlocks(App.numOfBlocks);
            run.setBlockSize(App.blockSizeKb);
            run.setTxSize(App.targetTxSizeKb());
            run.setDiskInfo(Util.getDiskInfo(dataDir));

            msg("disk info: (" + run.getDiskInfo() + ")");
            observer.showDiskInfo(run.getDiskInfo());

            for (int m = startFileNum; m < startFileNum + App.numOfMarks && !runner.isCancelled(); m++) {

                if (App.multiFile) {
                    testFile = new File(dataDir.getAbsolutePath()
                            + File.separator + "testdata" + m + ".jdm");
                }
                rMark = new DiskMark(READ);
                rMark.setMarkNum(m);
                long startTime = System.nanoTime();
                long totalBytesReadInMark = 0;

                try (RandomAccessFile rAccFile = new RandomAccessFile(testFile, "r")) {
                    for (int b = 0; b < numOfBlocks; b++) {
                        if (App.blockSequence == DiskRun.BlockSequence.RANDOM) {
                            rAccFile.seek((long) Util.randInt(0, numOfBlocks - 1) * blockSize);
                        } else {
                            rAccFile.seek((long) b * blockSize);
                        }
                        rAccFile.readFully(blockArr, 0, blockSize);
                        totalBytesReadInMark += blockSize;
                        rUnitsComplete++;
                        unitsComplete = rUnitsComplete + wUnitsComplete;
                        percentComplete = (float) unitsComplete / (float) unitsTotal * 100f;
                        observer.updateProgress((int) percentComplete);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    String emsg = "May not have done Write Benchmarks, so no data available to read. " + ex.getMessage();
                    observer.handleReadError(emsg);  // <-- NEW method on observer, see below
                    return false;
                }

                long endTime = System.nanoTime();
                double sec = (double)(endTime - startTime) / 1_000_000_000.0;
                double mbRead = (double) totalBytesReadInMark / (double) MEGABYTE;
                rMark.setBwMbSec(mbRead / sec);
                msg("m:" + m + " READ IO is " + rMark.getBwMbSec() + " MB/s");
                App.updateMetrics(rMark);
                observer.publishMark(rMark);

                run.setRunMax(rMark.getCumMax());
                run.setRunMin(rMark.getCumMin());
                run.setRunAvg(rMark.getCumAvg());
                run.setEndTime(new Date());
            }

            EntityManager em = EM.getEntityManager();
            em.getTransaction().begin();
            em.persist(run);
            em.getTransaction().commit();
            Gui.runPanel.addRun(run);
        }

        App.nextMarkNumber += App.numOfMarks;
        return true;
    }
}