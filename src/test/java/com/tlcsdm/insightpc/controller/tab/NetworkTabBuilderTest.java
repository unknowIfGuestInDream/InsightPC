package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkTabBuilderTest {

    @Test
    void testSafeDelta() {
        assertEquals(20L, NetworkTabBuilder.safeDelta(120L, 100L));
        assertEquals(0L, NetworkTabBuilder.safeDelta(50L, 100L));
    }

    @Test
    void testFormatSpeedText() {
        assertEquals("1.0 KB/s", NetworkTabBuilder.formatSpeedText(1024L, 1.0));
        assertEquals("768 B/s", NetworkTabBuilder.formatSpeedText(1536L, 2.0));
        assertEquals("0 B/s", NetworkTabBuilder.formatSpeedText(0L, 1.0));
        assertEquals("0 B/s", NetworkTabBuilder.formatSpeedText(100L, 0.0));
    }

    @Test
    void testJoinAddressArray() {
        assertEquals("N/A", NetworkTabBuilder.joinAddressArray(null));
        assertEquals("N/A", NetworkTabBuilder.joinAddressArray(new String[0]));
        assertEquals("192.168.0.1, 10.0.0.1", NetworkTabBuilder.joinAddressArray(new String[]{"192.168.0.1", "10.0.0.1"}));
    }

    @Test
    void testToText() {
        assertEquals("5", NetworkTabBuilder.toText(5));
        assertEquals("N/A", NetworkTabBuilder.toText(-1));
    }

    @Test
    void testNormalizeFieldValue() {
        assertEquals("N/A", NetworkTabBuilder.normalizeFieldValue(null));
        assertEquals("N/A", NetworkTabBuilder.normalizeFieldValue("   "));
        assertEquals("example", NetworkTabBuilder.normalizeFieldValue("  example  "));
    }
}
