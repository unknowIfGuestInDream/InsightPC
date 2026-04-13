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
    }

    @Test
    void testFormatFrequencyText() {
        assertEquals("2.00", CpuTabBuilder.formatFrequencyText(new long[]{2_000_000_000L, 2_000_000_000L}, 0));
        assertEquals("3.00", CpuTabBuilder.formatFrequencyText(new long[0], 3_000_000_000L));
        assertEquals("N/A", CpuTabBuilder.formatFrequencyText(null, 0));
    }

    @Test
    void testFormatMetricTexts() {
        assertEquals("123", CpuTabBuilder.formatCounterText(123));
        assertEquals("N/A", CpuTabBuilder.formatCounterText(-1));
        assertEquals("42.5°C", CpuTabBuilder.formatTemperatureText(42.5));
        assertEquals("0.0°C", CpuTabBuilder.formatTemperatureText(-5));
        assertEquals("1.2", CpuTabBuilder.formatVoltageText(1.2));
        assertEquals("0.0", CpuTabBuilder.formatVoltageText(-1));
        assertEquals("[1200, 1300]", CpuTabBuilder.formatFanSpeedsText(new int[]{1200, 1300}));
        assertEquals("[]", CpuTabBuilder.formatFanSpeedsText(null));
    }
}
