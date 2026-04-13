package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoryTabBuilderTest {

    @Test
    void testCalculateUsageWithZeroOrNegativeValues() {
        assertEquals(0.0, MemoryTabBuilder.calculateUsage(1, 0));
        assertEquals(0.0, MemoryTabBuilder.calculateUsage(-1, 100));
    }

    @Test
    void testCalculateUsageWithinRange() {
        assertEquals(0.5, MemoryTabBuilder.calculateUsage(50, 100));
    }

    @Test
    void testCalculateUsageCapsAtOne() {
        assertEquals(1.0, MemoryTabBuilder.calculateUsage(150, 100));
    }

    @Test
    void testFormatPercentText() {
        assertEquals("88%", MemoryTabBuilder.formatPercentText(0.88));
        assertEquals("0%", MemoryTabBuilder.formatPercentText(0.0));
    }
}
