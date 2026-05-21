package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
/** this is an interface that the observers will all implement.
 * The benefit of this is the standardization it causes */
public interface BenchmarkRunObserver {
    void addRun(DiskRun run);
}
