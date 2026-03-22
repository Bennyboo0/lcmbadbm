package edu.touro.mco152.bm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *this is unit tests for DiskMark
 */
public class DiskMarkTest {

    private DiskMark writeMark;
    private DiskMark readMark;

    @BeforeEach
    void setUp() {
        writeMark = new DiskMark(DiskMark.MarkType.WRITE);
        readMark  = new DiskMark(DiskMark.MarkType.READ);
    }

    /**
     *Right-BICEP: RIGHT — correct result.
     *a newly constructed DiskMark must report the exact MarkType it was given.
     */
    @Test
    void constructor_writeType_returnsWriteType() {
        assertEquals(DiskMark.MarkType.WRITE, writeMark.getType(),
                "DiskMark constructed as WRITE must return WRITE type");
    }

    @Test
    void constructor_readType_returnsReadType() {
        assertEquals(DiskMark.MarkType.READ, readMark.getType(),
                "DiskMark constructed as READ must return READ type");
    }

    /**
     *Right-BICEP: INVERSE RELATIONSHIP — set a value, then retrieve it.
     *setBwMbSec followed by getBwMbSec must return exactly the stored value.
     *this is the inverse (write/read) check.
     */
    @Test
    void setBwMbSec_thenGet_returnsSameValue() {
        writeMark.setBwMbSec(123.456);
        assertEquals(123.456, writeMark.getBwMbSec(), 0.0001,
                "getBwMbSec must return exactly what was set by setBwMbSec");
    }

    /**
     *Right-BICEP: INVERSE RELATIONSHIP — same inverse check for cumAvg.
     *setCumAvg followed by getCumAvg must round-trip correctly.
     */
    @Test
    void setCumAvg_thenGet_returnsSameValue() {
        writeMark.setCumAvg(88.8);
        assertEquals(88.8, writeMark.getCumAvg(), 0.0001,
                "getCumAvg must return exactly what was set by setCumAvg");
    }

    /**
     *Right-BICEP: BOUNDARY condition.
     *CORRECT Boundary — CONFORMANCE: getBwMbSecAsString() must return a string
     *that conforms to the ###.### decimal format — no letters, not empty,
     *and parseable as a double.
     */
    @Test
    void getBwMbSecAsString_validSpeed_conformsToDecimalFormat() {
        writeMark.setBwMbSec(256.5);
        String result = writeMark.getBwMbSecAsString();

        assertNotNull(result, "Formatted string must not be null");
        assertFalse(result.isEmpty(), "Formatted string must not be empty");
        assertDoesNotThrow(() -> Double.parseDouble(result),
                "Formatted string must be parseable as a double: " + result);
    }

    /**
     *Right-BICEP: BOUNDARY condition.
     *CORRECT Boundary — RANGE: bwMbSec of 0.0 is the minimum meaningful value.
     *the string representation of 0 should be "0", not null or blank.
     */
    @Test
    void getBwMbSecAsString_zeroSpeed_returnsZeroString() {
        writeMark.setBwMbSec(0.0);
        String result = writeMark.getBwMbSecAsString();
        assertNotNull(result);
        assertEquals("0", result,
                "Zero bwMbSec should format as '0'");
    }

    /**
     *Right-BICEP: ERROR condition.
     *setMarkNum with a negative value — DiskMark makes no validation promises,
     *but it must at minimum not throw and must store/retrieve the value faithfully.
     */
    @Test
    void setMarkNum_negativeValue_storedWithoutException() {
        assertDoesNotThrow(() -> writeMark.setMarkNum(-1));
        assertEquals(-1, writeMark.getMarkNum(),
                "Negative markNum should be stored without modification");
    }
}