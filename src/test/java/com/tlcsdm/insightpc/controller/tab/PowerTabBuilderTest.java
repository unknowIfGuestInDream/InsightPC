package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PowerTabBuilderTest {

    @Test
    void testNormalizeUsage() {
        assertEquals(0.0, PowerTabBuilder.normalizeUsage(Double.NaN));
        assertEquals(0.0, PowerTabBuilder.normalizeUsage(Double.NEGATIVE_INFINITY));
        assertEquals(0.0, PowerTabBuilder.normalizeUsage(-0.2));
        assertEquals(0.2, PowerTabBuilder.normalizeUsage(0.2));
        assertEquals(1.0, PowerTabBuilder.normalizeUsage(1.2));
    }

    @Test
    void testFormatPercentText() {
        assertEquals("0%", PowerTabBuilder.formatPercentText(-0.1));
        assertEquals("25%", PowerTabBuilder.formatPercentText(0.25));
        assertEquals("100%", PowerTabBuilder.formatPercentText(1.5));
    }

    @Test
    void testNormalizeFieldValue() {
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue(null));
        assertEquals(I18N.get("power.notAvailable"), AbstractTabBuilder.normalizeFieldValue("   "));
        assertEquals("Battery", AbstractTabBuilder.normalizeFieldValue("  Battery "));
    }
}
