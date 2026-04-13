package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryTabBuilderTest {

    @Test
    void testCalculateUsageWithZeroOrNegativeValues() {
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(1, 0));
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(-1, 100));
    }

    @Test
    void testCalculateUsageWithinRange() {
        assertEquals(0.5, AbstractTabBuilder.calculateUsage(50, 100));
    }

    @Test
    void testCalculateUsageCapsAtOne() {
        assertEquals(1.0, AbstractTabBuilder.calculateUsage(150, 100));
    }

    @Test
    void testFormatPercentText() {
        assertEquals("88%", AbstractTabBuilder.formatPercentText(0.88));
        assertEquals("0%", AbstractTabBuilder.formatPercentText(0.0));
    }

    @Test
    void testIsLowUsageForOverlayText() {
        assertTrue(MemoryTabBuilder.isLowUsageForOverlayText(0.49));
        assertFalse(MemoryTabBuilder.isLowUsageForOverlayText(0.5));
    }

    @Test
    void testNormalizeFieldValue() {
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue(null));
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue("   "));
        assertEquals("8 GB", AbstractTabBuilder.normalizeFieldValue(" 8 GB "));
    }
}
