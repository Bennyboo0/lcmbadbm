package edu.touro.mco152.bm.commands;

import edu.touro.mco152.bm.*;
import edu.touro.mco152.bm.persist.DiskRun;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.KILOBYTE;
import static edu.touro.mco152.bm.App.MEGABYTE;
import static edu.touro.mco152.bm.DiskMark.MarkType.WRITE;

/**
 * Concrete implementation of BenchmarkCommand handling sequential or random
 * disk write benchmark operations.
 * Encapsulates the complete life cycle of writing blocks to a temporary file and
 * broadcasting performance updates back to an observer.
 */
public class WriteBM implements BenchmarkCommand {

    private final BenchmarkRunner runner;
    private final BenchmarkObserver observer;
    private final int numMarks;
    private final int numBlocks;
    private final int blockSizeKb;
    private final DiskRun.BlockSequence blockSequence;
    private final boolean multiFile;
    private final boolean writeSyncEnable;

    //see how this ist typed specifically to BenchmarkRunObserver interface
    private final List<BenchmarkRunObserver> runObservers = new ArrayList<>();

    public WriteBM(BenchmarkRunner runner, BenchmarkObserver observer,
                   int numMarks, int numBlocks, int blockSizeKb,
                   DiskRun.BlockSequence blockSequence,
                   boolean multiFile, boolean writeSyncEnable) {
        this.runner = runner;
        this.observer = observer;
        this.numMarks = numMarks;
        this.numBlocks = numBlocks;
        this.blockSizeKb = blockSizeKb;
        this.blockSequence = blockSequence;
        this.multiFile = multiFile;
        this.writeSyncEnable = writeSyncEnable;
    }

    /**
     * Registers a post-benchmark activity observer.
     */
    public void registerObserver(BenchmarkRunObserver observer) {
        runObservers.add(observer);
    }

    @Override
    public boolean execute() {
        int wUnitsComplete = 0, rUnitsComplete = 0, unitsComplete;
        int unitsTotal = numBlocks * numMarks;
        float percentComplete;

        int blockSize = blockSizeKb * KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) blockArr[b] = (byte) 0xFF;
        }

        DiskMark wMark;
        int startFileNum = App.nextMarkNumber;

        DiskRun run = new DiskRun(DiskRun.IOMode.WRITE, blockSequence);
        run.setNumMarks(numMarks);
        run.setNumBlocks(numBlocks);
        run.setBlockSize(blockSizeKb);
        run.setTxSize((long) numBlocks * blockSizeKb);
        run.setDiskInfo(Util.getDiskInfo(App.dataDir));

        App.msg("disk info: (" + run.getDiskInfo() + ")");
        observer.showDiskInfo(run.getDiskInfo());

        if (!multiFile) {
            App.testFile = new File(App.dataDir.getAbsolutePath()
                    + File.separator + "testdata.jdm");
        }

        for (int m = startFileNum; m < startFileNum + numMarks && !runner.isCancelled(); m++) {

            if (multiFile) {
                App.testFile = new File(App.dataDir.getAbsolutePath()
                        + File.separator + "testdata" + m + ".jdm");
            }

            wMark = new DiskMark(WRITE);
            wMark.setMarkNum(m);
            long startTime = System.nanoTime();
            long totalBytesWrittenInMark = 0;
            String mode = writeSyncEnable ? "rwd" : "rw";

            try (RandomAccessFile rAccFile = new RandomAccessFile(App.testFile, mode)) {
                for (int b = 0; b < numBlocks; b++) {
                    if (blockSequence == DiskRun.BlockSequence.RANDOM) {
                        rAccFile.seek((long) Util.randInt(0, numBlocks - 1) * blockSize);
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
            App.msg("m:" + m + " write IO is " + wMark.getBwMbSecAsString() + " MB/s");
            App.updateMetrics(wMark);
            observer.publishMark(wMark);

            run.setRunMax(wMark.getCumMax());
            run.setRunMin(wMark.getCumMin());
            run.setRunAvg(wMark.getCumAvg());
            run.setEndTime(new Date());
        }

        // now notify all dynamic post-benchmark observers using your clean interface!
        for (BenchmarkRunObserver runObs : runObservers) {
            runObs.addRun(run);
        }

        return true;
    }
}