package com.tlcsdm.insightpc.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced tests for SystemInfoService covering edge cases in formatBytes/formatUptime
 * and OSHI data accessors.
 */
class SystemInfoServiceEnhancedTest {

    private static SystemInfoService service;

    @BeforeAll
    static void setUp() {
        service = new SystemInfoService();
    }

    // --- formatBytes edge cases ---

    @Test
    void testFormatBytesNegative() {
        assertEquals("N/A", SystemInfoService.formatBytes(-1));
        assertEquals("N/A", SystemInfoService.formatBytes(-100));
        assertEquals("N/A", SystemInfoService.formatBytes(Long.MIN_VALUE));
    }

    @Test
    void testFormatBytesZero() {
        assertEquals("0 B", SystemInfoService.formatBytes(0));
    }

    @Test
    void testFormatBytesSingleByte() {
        assertEquals("1 B", SystemInfoService.formatBytes(1));
    }

    @Test
    void testFormatBytesMaxBytesBeforeKB() {
        assertEquals("1023 B", SystemInfoService.formatBytes(1023));
    }

    @Test
    void testFormatBytesExactKB() {
        assertEquals("1.0 KB", SystemInfoService.formatBytes(1024));
    }

    @Test
    void testFormatBytesExactMB() {
        assertEquals("1.0 MB", SystemInfoService.formatBytes(1024 * 1024));
    }

    @Test
    void testFormatBytesExactGB() {
        assertEquals("1.0 GB", SystemInfoService.formatBytes(1024L * 1024 * 1024));
    }

    @Test
    void testFormatBytesExactTB() {
        assertEquals("1.0 TB", SystemInfoService.formatBytes(1024L * 1024 * 1024 * 1024));
    }

    @ParameterizedTest
    @CsvSource({
        "512, 512 B",
        "1536, 1.5 KB",
        "1048576, 1.0 MB",
        "1073741824, 1.0 GB",
        "1099511627776, 1.0 TB"
    })
    void testFormatBytesParameterized(long bytes, String expected) {
        assertEquals(expected, SystemInfoService.formatBytes(bytes));
    }

    @Test
    void testFormatBytesLargeValue() {
        // 5 TB
        long fiveTB = 5L * 1024 * 1024 * 1024 * 1024;
        assertEquals("5.0 TB", SystemInfoService.formatBytes(fiveTB));
    }

    // --- formatUptime edge cases ---

    @Test
    void testFormatUptimeZero() {
        assertEquals("0m 0s", SystemInfoService.formatUptime(0));
    }

    @Test
    void testFormatUptimeOneSecond() {
        assertEquals("0m 1s", SystemInfoService.formatUptime(1));
    }

    @Test
    void testFormatUptimeOneMinute() {
        assertEquals("1m 0s", SystemInfoService.formatUptime(60));
    }

    @Test
    void testFormatUptimeOneHour() {
        assertEquals("1h 0m 0s", SystemInfoService.formatUptime(3600));
    }

    @Test
    void testFormatUptimeOneDay() {
        assertEquals("1d 0h 0m 0s", SystemInfoService.formatUptime(86400));
    }

    @Test
    void testFormatUptimeComplex() {
        // 1 day, 2 hours, 3 minutes, 4 seconds = 93784
        assertEquals("1d 2h 3m 4s", SystemInfoService.formatUptime(93784));
    }

    @Test
    void testFormatUptime59Seconds() {
        assertEquals("0m 59s", SystemInfoService.formatUptime(59));
    }

    @Test
    void testFormatUptimeHoursWithoutDays() {
        // 3 hours, 15 minutes, 30 seconds
        assertEquals("3h 15m 30s", SystemInfoService.formatUptime(3 * 3600 + 15 * 60 + 30));
    }

    // --- OSHI data accessors ---

    @Test
    void testGetOperatingSystemNotNull() {
        assertNotNull(service.getOperatingSystem());
    }

    @Test
    void testGetHardwareNotNull() {
        assertNotNull(service.getHardware());
    }

    @Test
    void testGetProcessorHasPositiveCores() {
        assertTrue(service.getProcessor().getPhysicalProcessorCount() > 0);
        assertTrue(service.getProcessor().getLogicalProcessorCount() > 0);
    }

    @Test
    void testGetMemoryTotalPositive() {
        assertTrue(service.getMemory().getTotal() > 0);
        assertTrue(service.getMemory().getAvailable() >= 0);
        assertTrue(service.getMemory().getAvailable() <= service.getMemory().getTotal());
    }

    @Test
    void testGetDiskStoresNotNull() {
        assertNotNull(service.getDiskStores());
    }

    @Test
    void testGetNetworkInterfacesNotNull() {
        assertNotNull(service.getNetworkInterfaces());
    }

    @Test
    void testGetComputerSystemNotNull() {
        assertNotNull(service.getComputerSystem());
        assertNotNull(service.getComputerSystem().getManufacturer());
        assertNotNull(service.getComputerSystem().getModel());
    }

    @Test
    void testGetOsFamilyNotBlank() {
        String family = service.getOsFamily();
        assertNotNull(family);
        assertFalse(family.isBlank());
    }

    @Test
    void testGetOsVersionInfoNotBlank() {
        String version = service.getOsVersionInfo();
        assertNotNull(version);
        assertFalse(version.isBlank());
    }

    @Test
    void testGetOsManufacturerNotNull() {
        assertNotNull(service.getOsManufacturer());
    }

    @Test
    void testGetSystemUptimePositive() {
        assertTrue(service.getSystemUptime() > 0);
    }

    @Test
    void testGetProcessCountPositive() {
        assertTrue(service.getProcessCount() > 0);
    }

    @Test
    void testGetThreadCountPositive() {
        assertTrue(service.getThreadCount() > 0);
    }

    @Test
    void testGetPowerSourcesNotNull() {
        assertNotNull(service.getPowerSources());
    }

    @Test
    void testGetUsbDevicesNotNull() {
        assertNotNull(service.getUsbDevices());
    }

    @Test
    void testGetSensorsNotNull() {
        assertNotNull(service.getSensors());
    }

    @Test
    void testGetGraphicsCardsNotNull() {
        assertNotNull(service.getGraphicsCards());
    }

    @Test
    void testGetSoundCardsNotNull() {
        assertNotNull(service.getSoundCards());
    }

    @Test
    void testGetDisplaysNotNull() {
        assertNotNull(service.getDisplays());
    }
}
