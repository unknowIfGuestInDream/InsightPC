package com.tlcsdm.insightpc.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SystemInfoService class.
 */
class SystemInfoServiceTest {

    private static SystemInfoService service;

    @BeforeAll
    static void setUp() {
        service = new SystemInfoService();
    }

    @Test
    void testGetOperatingSystem() {
        assertNotNull(service.getOperatingSystem(), "OS should not be null");
    }

    @Test
    void testGetHardware() {
        assertNotNull(service.getHardware(), "Hardware should not be null");
    }

    @Test
    void testGetProcessor() {
        assertNotNull(service.getProcessor(), "Processor should not be null");
        assertTrue(service.getProcessor().getLogicalProcessorCount() > 0,
            "Should have at least one logical processor");
    }

    @Test
    void testGetMemory() {
        assertNotNull(service.getMemory(), "Memory should not be null");
        assertTrue(service.getMemory().getTotal() > 0, "Total memory should be positive");
    }

    @Test
    void testGetDiskStores() {
        assertNotNull(service.getDiskStores(), "Disk stores should not be null");
    }

    @Test
    void testGetNetworkInterfaces() {
        assertNotNull(service.getNetworkInterfaces(), "Network interfaces should not be null");
    }

    @Test
    void testGetComputerSystem() {
        assertNotNull(service.getComputerSystem(), "Computer system should not be null");
    }

    @Test
    void testGetOsFamily() {
        String family = service.getOsFamily();
        assertNotNull(family, "OS family should not be null");
        assertFalse(family.isEmpty(), "OS family should not be empty");
    }

    @Test
    void testGetSystemUptime() {
        assertTrue(service.getSystemUptime() > 0, "System uptime should be positive");
    }

    @Test
    void testGetProcessCount() {
        assertTrue(service.getProcessCount() > 0, "Should have at least one process");
    }

    @Test
    void testGetThreadCount() {
        assertTrue(service.getThreadCount() > 0, "Should have at least one thread");
    }

    @Test
    void testGetPowerSources() {
        assertNotNull(service.getPowerSources(), "Power sources should not be null");
    }

    @Test
    void testGetUsbDevices() {
        assertNotNull(service.getUsbDevices(), "USB devices should not be null");
    }

    @Test
    void testGetSensors() {
        assertNotNull(service.getSensors(), "Sensors should not be null");
    }

    @Test
    void testGetGraphicsCards() {
        assertNotNull(service.getGraphicsCards(), "Graphics cards should not be null");
    }

    @Test
    void testGetSoundCards() {
        assertNotNull(service.getSoundCards(), "Sound cards should not be null");
    }

    @Test
    void testFormatBytes() {
        assertEquals("N/A", SystemInfoService.formatBytes(-1));
        assertEquals("0 B", SystemInfoService.formatBytes(0));
        assertEquals("512 B", SystemInfoService.formatBytes(512));
        assertEquals("1.0 KB", SystemInfoService.formatBytes(1024));
        assertEquals("1.0 MB", SystemInfoService.formatBytes(1024 * 1024));
        assertEquals("1.0 GB", SystemInfoService.formatBytes(1024L * 1024 * 1024));
        assertEquals("1.0 TB", SystemInfoService.formatBytes(1024L * 1024 * 1024 * 1024));
    }

    @Test
    void testFormatUptime() {
        assertEquals("0m 0s", SystemInfoService.formatUptime(0));
        assertEquals("1m 30s", SystemInfoService.formatUptime(90));
        assertEquals("1h 0m 0s", SystemInfoService.formatUptime(3600));
        assertEquals("1d 2h 3m 4s", SystemInfoService.formatUptime(93784));
    }
}
