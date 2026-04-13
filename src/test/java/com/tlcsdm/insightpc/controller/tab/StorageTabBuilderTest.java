package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageTabBuilderTest {

    @Test
    void testCalculateUsageWithZeroOrNegativeTotal() {
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(100, 0));
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(100, -1));
    }

    @Test
    void testCalculateUsageWithinRange() {
        assertEquals(0.5, AbstractTabBuilder.calculateUsage(50, 100));
        assertEquals(1.0, AbstractTabBuilder.calculateUsage(150, 100));
    }

    @Test
    void testFormatPercentText() {
        assertEquals("25%", AbstractTabBuilder.formatPercentText(0.25));
        assertEquals("100%", AbstractTabBuilder.formatPercentText(1.0));
    }

    @Test
    void testNormalizeFieldValue() {
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue(null));
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue("   "));
        assertEquals("SERIAL-001", AbstractTabBuilder.normalizeFieldValue("  SERIAL-001  "));
    }
}
