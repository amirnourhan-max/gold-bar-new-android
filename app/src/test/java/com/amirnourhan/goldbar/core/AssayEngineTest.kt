package com.amirnourhan.goldbar.core

import org.junit.Assert.*
import org.junit.Test

class AssayEngineTest {
    @Test fun summarizeMatchesWindowsFormula() {
        val s = AssayEngine.summarize(listOf(MeltEntry(weight = 100.0, assay = 750.0), MeltEntry(weight = 50.0, assay = 740.0)))
        assertEquals(150.0, s.weight, 0.000001)
        assertEquals(746.666666, s.averageAssay, 0.0001)
    }
    @Test fun increaseRoundsDownLikeWindows() {
        val s = AssaySummary(150.0, 112000.0, 746.666666, 2)
        val r = AssayEngine.increase(s, 750.0, 995.0)
        assertEquals(2.0, r.requiredBar, 0.1)
    }
    @Test fun alloyMatchesWindowsFormula() {
        val s = AssaySummary(100.0, 75000.0, 750.0, 1)
        val r = AssayEngine.alloy(s, 740.0, 63.21, 100.0)
        assertTrue(r.totalAlloyRequired > 1.3)
        assertTrue(r.silverRequired > 0.8)
    }
}
