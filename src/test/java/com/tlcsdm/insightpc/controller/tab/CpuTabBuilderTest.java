package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpuTabBuilderTest {

    @Test
    void testNormalizeLoad() {
        assertEquals(0.0, CpuTabBuilder.normalizeLoad(Double.NaN));
        assertEquals(0.0, CpuTabBuilder.normalizeLoad(Double.POSITIVE_INFINITY));
        assertEquals(0.0, CpuTabBuilder.normalizeLoad(-0.1));
        assertEquals(0.0, CpuTabBuilder.normalizeLoad(0.0));
        assertEquals(0.25, CpuTabBuilder.normalizeLoad(0.25));
        assertEquals(1.0, CpuTabBuilder.normalizeLoad(1.5));
    }

    @Test
    void testFormatSystemUsageText() {
        assertEquals("12.3%", CpuTabBuilder.formatSystemUsageText(0.123));
        assertEquals("100.0%", CpuTabBuilder.formatSystemUsageText(1.0));
    }

    @Test
    void testFormatCoreUsageText() {
        assertEquals("12.35%", CpuTabBuilder.formatCoreUsageText(0.12345));
        assertEquals("100.00%", CpuTabBuilder.formatCoreUsageText(1.0));
    }

    @Test
    void testCalculateUnusedLoad() {
        assertEquals(1.0, CpuTabBuilder.calculateUnusedLoad(0.0));
        assertEquals(0.75, CpuTabBuilder.calculateUnusedLoad(0.25));
        assertEquals(0.0, CpuTabBuilder.calculateUnusedLoad(1.0));
        assertEquals(0.0, CpuTabBuilder.calculateUnusedLoad(2.0));
    }
}
