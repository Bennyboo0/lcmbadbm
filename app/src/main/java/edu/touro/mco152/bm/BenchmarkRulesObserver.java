package edu.touro.mco152.bm;

import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.externalsys.SlackManager;

/**
 * This class is to follow the advice in 7c on the hw doc. It allows for all rules to be put in there.
 * As you can see, we only check 1 rule, which if the read benchmark max time exceeds average by > 3%
 */
public class BenchmarkRulesObserver implements BenchmarkRunObserver {
    private final SlackManager slackManager;

    public BenchmarkRulesObserver() {
        this.slackManager = new SlackManager("BadBM");
    }


    @Override
    public void addRun(DiskRun run) {
        if (run.getIoMode() == DiskRun.IOMode.READ) {
            // If max time > 3% higher than average time
            double maxTime = run.getRunMax();
            double avgTime = run.getRunAvg();


            if (avgTime > 0 && maxTime > (avgTime * 1.03)) { //we have the first condition so that this doesn't do off when a run is uninitialized
                String msg = String.format(":warning: Read Max (%.2f ms) exceeded 3%% of Avg (%.2f ms)!",
                        maxTime, avgTime);
                slackManager.postMsg2OurChannel(msg);
            }
        }
        //and so, like the hw requests for, future rules can easily be appended right here without changing core benchmark code

    }
}