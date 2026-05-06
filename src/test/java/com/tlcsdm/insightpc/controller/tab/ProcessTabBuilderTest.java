package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessTabBuilderTest {

    @Test
    void testResolveProcessLimit() {
        assertEquals(1, ProcessTabBuilder.resolveProcessLimit(0));
        assertEquals(1, ProcessTabBuilder.resolveProcessLimit(-2));
        assertEquals(128, ProcessTabBuilder.resolveProcessLimit(128));
    }

    @Test
    void testGetRefreshIntervalSeconds() {
        assertEquals(2, ProcessTabBuilder.getRefreshIntervalSeconds(null));
        assertEquals(2, ProcessTabBuilder.getRefreshIntervalSeconds(0));
        assertEquals(5, ProcessTabBuilder.getRefreshIntervalSeconds(5));
    }

    @Test
    void testCalculateCpuPercent() {
        assertEquals(0.0,
            ProcessTabBuilder.calculateCpuPercent(Double.NaN, ProcessTabBuilder.CpuPercentScope.SYSTEM, 4));
        assertEquals(50.0,
            ProcessTabBuilder.calculateCpuPercent(0.5, ProcessTabBuilder.CpuPercentScope.ONE_PROCESSOR, 8));
        assertEquals(100.0,
            ProcessTabBuilder.calculateCpuPercent(2.0, ProcessTabBuilder.CpuPercentScope.ONE_PROCESSOR, 8));
        assertEquals(12.5,
            ProcessTabBuilder.calculateCpuPercent(0.5, ProcessTabBuilder.CpuPercentScope.SYSTEM, 4));
        assertEquals(50.0,
            ProcessTabBuilder.calculateCpuPercent(0.5, ProcessTabBuilder.CpuPercentScope.SYSTEM, 0));
    }

    @Test
    void testCalculateMemoryPercent() {
        assertEquals(0.0, ProcessTabBuilder.calculateMemoryPercent(0, 1024));
        assertEquals(0.0, ProcessTabBuilder.calculateMemoryPercent(1024, 0));
        assertEquals(50.0, ProcessTabBuilder.calculateMemoryPercent(512, 1024));
        assertEquals(100.0, ProcessTabBuilder.calculateMemoryPercent(2048, 1024));
    }

    @Test
    void testSelectResidentMemory() {
        assertEquals(1024, ProcessTabBuilder.selectResidentMemory("Linux", 1024, 512));
        assertEquals(1024, ProcessTabBuilder.selectResidentMemory("Mac OS X", 1024, 512));
        assertEquals(1024, ProcessTabBuilder.selectResidentMemory("AIX", 1024, 512));
        assertEquals(512, ProcessTabBuilder.selectResidentMemory("Windows", 1024, 512));
        assertEquals(512, ProcessTabBuilder.selectResidentMemory("Windows 11", 1024, 512));
        assertEquals(512, ProcessTabBuilder.selectResidentMemory("windows", 1024, 512));
        assertEquals(1024, ProcessTabBuilder.selectResidentMemory("", 1024, 512));
        assertEquals(1024, ProcessTabBuilder.selectResidentMemory(null, 1024, 512));
    }

    @Test
    void testFormatPercentValue() {
        assertEquals("0.0%", ProcessTabBuilder.formatPercentValue(0));
        assertEquals("12.3%", ProcessTabBuilder.formatPercentValue(12.34));
    }

    @Test
    void testCreateSummaryItemText() {
        assertEquals("Process Count: 123", ProcessTabBuilder.createSummaryItemText("Process Count", 123));
    }

    @Test
    void testProcessIconColumnWidth() {
        assertEquals(54d, ProcessTabBuilder.PROCESS_ICON_COLUMN_WIDTH);
    }

    @Test
    void testCreateProcessIconCacheKey() {
        assertEquals("C:/Program Files/App/app.exe",
            ProcessTabBuilder.createProcessIconCacheKey("C:/Program Files/App/app.exe", "app"));
        assertEquals("app",
            ProcessTabBuilder.createProcessIconCacheKey("   ", "app"));
        assertEquals("",
            ProcessTabBuilder.createProcessIconCacheKey(null, null));
    }

    @Test
    void testCreateSortComparator() {
        List<ProcessTabBuilder.ProcessRow> rows = new ArrayList<>();
        rows.add(new ProcessTabBuilder.ProcessRow(null, 2, 0, 0, 10.0, 30.0, 20.0, "", "", "b"));
        rows.add(new ProcessTabBuilder.ProcessRow(null, 1, 0, 0, 30.0, 20.0, 10.0, "", "", "a"));
        rows.add(new ProcessTabBuilder.ProcessRow(null, 3, 0, 0, 20.0, 10.0, 40.0, "", "", "c"));

        rows.sort(ProcessTabBuilder.createSortComparator(ProcessTabBuilder.ProcessSort.CPU));
        assertEquals(1, rows.get(0).pid());

        rows.sort(ProcessTabBuilder.createSortComparator(ProcessTabBuilder.ProcessSort.CUMULATIVE_CPU));
        assertEquals(2, rows.get(0).pid());

        rows.sort(ProcessTabBuilder.createSortComparator(ProcessTabBuilder.ProcessSort.MEMORY));
        assertEquals(3, rows.get(0).pid());
    }
}
