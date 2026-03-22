package edu.touro.mco152.bm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *this is unit tests for pure calculation methods in App.
 */
public class AppTest {

    /**
     *this resets all static state before each test so tests are independent.
     */
    @BeforeEach
    void resetAppState() {
        App.blockSizeKb = 512;
        App.numOfBlocks = 32;
        App.numOfMarks = 25;
        App.wAvg = -1;
        App.wMax = -1;
        App.wMin = -1;
        App.rAvg = -1;
        App.rMax = -1;
        App.rMin = -1;
    }

    /**
     *Right-BICEP: RIGHT — correct result.
     *with default values (512 kb blocks, 32 blocks), the mark size should be exactly 16384 kb.
     */
    @Test
    void targetMarkSizeKb_defaultValues_returnsCorrectProduct() {
        long result = App.targetMarkSizeKb();
        assertEquals(512L * 32L, result,
                "targetMarkSizeKb should return blockSizeKb * numOfBlocks");
    }

    /**
     *Right-BICEP: BOUNDARY condition.
     *CORRECT Boundary — RANGE: lower bound of zero blocks.
     *when numOfBlocks is 0, mark size must be 0 regardless of block size.
     */
    @Test
    void targetMarkSizeKb_zeroBlocks_returnsZero() {
        App.numOfBlocks = 0;
        assertEquals(0L, App.targetMarkSizeKb(),
                "Zero blocks should produce zero mark size");
    }

    /**
     *Right-BICEP: BOUNDARY condition.
     *CORRECT Boundary — RANGE: lower bound of zero block size.
     *when blockSizeKb is 0, result must be 0.
     */
    @Test
    void targetMarkSizeKb_zeroBlockSize_returnsZero() {
        App.blockSizeKb = 0;
        assertEquals(0L, App.targetMarkSizeKb(),
                "Zero block size should produce zero mark size");
    }
    /**
     *Right-BICEP: RIGHT — correct results across multiple input combinations.
     *uses @ParameterizedTest to verify targetTxSizeKb = blockSizeKb * numOfBlocks * numOfMarks
     *for several realistic configurations.
     */
    @ParameterizedTest(name = "blockSizeKb={0}, numOfBlocks={1}, numOfMarks={2} => {3} kb")
    @CsvSource({
            "512, 32, 25, 409600",
            "128, 8,  10, 10240",
            "1024, 64, 5, 327680",
            "256, 16, 1,  4096"
    })
    void targetTxSizeKb_variousConfigs_returnsCorrectProduct(
            int blockSizeKb, int numOfBlocks, int numOfMarks, long expected) {
        App.blockSizeKb = blockSizeKb;
        App.numOfBlocks = numOfBlocks;
        App.numOfMarks = numOfMarks;
        assertEquals(expected, App.targetTxSizeKb(),
                "targetTxSizeKb must equal blockSizeKb * numOfBlocks * numOfMarks");
    }

    /**
     *Right-BICEP: CROSS-CHECK — two independent computations of the same value must agree.
     *targetTxSizeKb() should equal targetMarkSizeKb() * numOfMarks,
     *since a transaction is just numOfMarks marks.
     */
    @Test
    void targetTxSizeKb_equalsMark_timesNumOfMarks() {
        long txSize  = App.targetTxSizeKb();
        long markSize = App.targetMarkSizeKb();
        assertEquals(markSize * App.numOfMarks, txSize,
                "targetTxSizeKb must equal targetMarkSizeKb * numOfMarks");
    }

    /**
     *Right-BICEP: PERFORMANCE — method must complete 10,000 calls in under 100 ms.
     *targetTxSizeKb is called in the UI hot path; it must never be a bottleneck.
     */
    @Test
    void targetTxSizeKb_performance_tenThousandCallsUnder100ms() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000; i++) {
            App.targetTxSizeKb();
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 100,
                "10,000 calls to targetTxSizeKb should complete in < 100ms, took: " + elapsed + "ms");
    }

    /**
     * Right-BICEP: BOUNDARY condition.
     *CORRECT Boundary — CARDINALITY: after exactly ONE write mark is processed,
     *cumAvg must equal that single mark's bwMbSec (no averaging formula needed for n=1).
     *this specifically exercises the wAvg == -1 (first-run) branch in updateMetrics.
     */
    @Test
    void updateMetrics_firstWriteMark_avgEqualsBwMbSec() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.WRITE);
        mark.setMarkNum(1);
        mark.setBwMbSec(100.0);

        App.updateMetrics(mark);

        assertEquals(100.0, mark.getCumAvg(), 0.0001,
                "After exactly one mark, cumAvg must equal that mark's bwMbSec");
    }

    /**
     *Right-BICEP: BOUNDARY condition.
     *CORRECT Boundary — ORDERING: after multiple marks, cumMax >= cumAvg >= cumMin must always hold.
     *verifies the invariant relationship between the three running statistics.
     */
    @Test
    void updateMetrics_multipleWriteMarks_maxGeAvgGeMin() {
        double[] speeds = {50.0, 200.0, 75.0};
        DiskMark lastMark = null;

        for (int i = 0; i < speeds.length; i++) {
            DiskMark mark = new DiskMark(DiskMark.MarkType.WRITE);
            mark.setMarkNum(i + 1);
            mark.setBwMbSec(speeds[i]);
            App.updateMetrics(mark);
            lastMark = mark;
        }

        assertNotNull(lastMark);
        assertTrue(lastMark.getCumMax() >= lastMark.getCumAvg(),
                "cumMax must be >= cumAvg");
        assertTrue(lastMark.getCumAvg() >= lastMark.getCumMin(),
                "cumAvg must be >= cumMin");
    }

    /**
     *Right-BICEP: ERROR condition.
     *updateMetrics with bwMbSec = 0 (degenerate input) should not crash and
     *should set max/min/avg all to 0 on the first mark.
     */
    @Test
    void updateMetrics_zeroBwMbSec_doesNotThrow() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.WRITE);
        mark.setMarkNum(1);
        mark.setBwMbSec(0.0);

        assertDoesNotThrow(() -> App.updateMetrics(mark));
        assertEquals(0.0, mark.getCumAvg(), 0.0001,
                "Zero bwMbSec should produce zero cumAvg");
    }
}