package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageTabBuilderTest {

    @Test
    void testCalculateUsageWithZeroOrNegativeTotal() {
        assertEquals(0.0, StorageTabBuilder.calculateUsage(100, 0));
        assertEquals(0.0, StorageTabBuilder.calculateUsage(100, -1));
    }

    @Test
    void testCalculateUsageWithinRange() {
        assertEquals(0.5, StorageTabBuilder.calculateUsage(50, 100));
        assertEquals(1.0, StorageTabBuilder.calculateUsage(150, 100));
    }

    @Test
    void testFormatPercentText() {
        assertEquals("25%", StorageTabBuilder.formatPercentText(0.25));
        assertEquals("100%", StorageTabBuilder.formatPercentText(1.0));
    }
}
