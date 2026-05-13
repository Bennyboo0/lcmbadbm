package edu.touro.mco152.bm.commands;

import edu.touro.mco152.bm.*;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import jakarta.persistence.EntityManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.KILOBYTE;
import static edu.touro.mco152.bm.App.MEGABYTE;
import static edu.touro.mco152.bm.DiskMark.MarkType.READ;

public class ReadBM implements BenchmarkCommand {

    private final BenchmarkRunner runner;
    private final BenchmarkObserver observer;
    private final int numMarks;
    private final int numBlocks;
    private final int blockSizeKb;
    private final DiskRun.BlockSequence blockSequence;

    public ReadBM(BenchmarkRunner runner, BenchmarkObserver observer,
                  int numMarks, int numBlocks, int blockSizeKb,
                  DiskRun.BlockSequence blockSequence) {
        this.runner = runner;
        this.observer = observer;
        this.numMarks = numMarks;
        this.numBlocks = numBlocks;
        this.blockSizeKb = blockSizeKb;
        this.blockSequence = blockSequence;
    }

    @Override
    public boolean execute() {
        int wUnitsComplete = 0, rUnitsComplete = 0, unitsComplete;
        int unitsTotal = numBlocks * numMarks;  // read-only context
        float percentComplete;

        int blockSize = blockSizeKb * KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) blockArr[b] = (byte) 0xFF;
        }

        DiskMark rMark;
        int startFileNum = App.nextMarkNumber;

        DiskRun run = new DiskRun(DiskRun.IOMode.READ, blockSequence);
        run.setNumMarks(numMarks);
        run.setNumBlocks(numBlocks);
        run.setBlockSize(blockSizeKb);
        run.setTxSize((long) numBlocks * blockSizeKb);  // computed locally, not from App
        run.setDiskInfo(Util.getDiskInfo(App.dataDir));

        App.msg("disk info: (" + run.getDiskInfo() + ")");
        observer.showDiskInfo(run.getDiskInfo());

        for (int m = startFileNum; m < startFileNum + numMarks && !runner.isCancelled(); m++) {
            if (App.multiFile) {
                App.testFile = new File(App.dataDir.getAbsolutePath()
                        + File.separator + "testdata" + m + ".jdm");
            }

            rMark = new DiskMark(READ);
            rMark.setMarkNum(m);
            long startTime = System.nanoTime();
            long totalBytesReadInMark = 0;

            try (RandomAccessFile rAccFile = new RandomAccessFile(App.testFile, "r")) {
                for (int b = 0; b < numBlocks; b++) {
                    if (blockSequence == DiskRun.BlockSequence.RANDOM) {
                        rAccFile.seek((long) Util.randInt(0, numBlocks - 1) * blockSize);
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
                String emsg = "Test file not found — make sure you've run a Write benchmark first. " + ex.getMessage();
                observer.handleReadError(emsg);
                return false;
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                String emsg = "May not have done Write Benchmarks, so no data available to read. " + ex.getMessage();
                observer.handleReadError(emsg);
                return false;
            }

            long endTime = System.nanoTime();
            double sec = (double)(endTime - startTime) / 1_000_000_000.0;
            double mbRead = (double) totalBytesReadInMark / (double) MEGABYTE;
            rMark.setBwMbSec(mbRead / sec);
            App.msg("m:" + m + " READ IO is " + rMark.getBwMbSec() + " MB/s");
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
        observer.addRun(run);  // <-- NOT Gui.runPanel.addRun(run) directly
        return true;
    }
}