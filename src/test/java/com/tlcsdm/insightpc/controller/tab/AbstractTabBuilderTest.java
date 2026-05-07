package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractTabBuilderTest {

    @Test
    void testNormalizeFieldValueNull() {
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue(null));
    }

    @Test
    void testNormalizeFieldValueBlank() {
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue(""));
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue("   "));
    }

    @Test
    void testNormalizeFieldValueTrimsWhitespace() {
        assertEquals("hello", AbstractTabBuilder.normalizeFieldValue("  hello  "));
    }

    @Test
    void testFormatDiskTypeNullOrBlank() {
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.formatDiskType(null));
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.formatDiskType("   "));
    }

    @Test
    void testFormatDiskTypeTrimsWhitespace() {
        assertEquals("SSD", AbstractTabBuilder.formatDiskType("  SSD  "));
        assertEquals("Unknown", AbstractTabBuilder.formatDiskType("Unknown"));
    }

    @Test
    void testCalculateUsageZeroTotal() {
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(100, 0));
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(100, -1));
    }

    @Test
    void testCalculateUsageNormal() {
        assertEquals(0.5, AbstractTabBuilder.calculateUsage(50, 100));
        assertEquals(0.25, AbstractTabBuilder.calculateUsage(25, 100));
    }

    @Test
    void testCalculateUsageCapsAtOne() {
        assertEquals(1.0, AbstractTabBuilder.calculateUsage(200, 100));
    }

    @Test
    void testCalculateUsageNegativeUsed() {
        assertEquals(0.0, AbstractTabBuilder.calculateUsage(-10, 100));
    }

    @Test
    void testFormatPercentText() {
        assertEquals("0%", AbstractTabBuilder.formatPercentText(0.0));
        assertEquals("50%", AbstractTabBuilder.formatPercentText(0.50));
        assertEquals("100%", AbstractTabBuilder.formatPercentText(1.0));
        assertEquals("88%", AbstractTabBuilder.formatPercentText(0.88));
    }
}
