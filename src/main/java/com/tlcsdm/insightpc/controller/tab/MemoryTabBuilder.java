package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import oshi.hardware.GlobalMemory;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Builds the Memory tab showing physical and virtual memory info.
 */
public class MemoryTabBuilder extends AbstractTabBuilder {

    public MemoryTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.memory"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignM.MEMORY));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Physical memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.physical")));
        MemoryUsagePanel physicalPanel = createUsagePanel(38);
        content.getChildren().add(physicalPanel.container());

        // Virtual memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.virtual")));
        MemoryUsagePanel virtualPanel = createUsagePanel(26);
        content.getChildren().add(virtualPanel.container());

        // Swap section
        content.getChildren().add(createSectionLabel(I18N.get("memory.swap")));
        MemoryUsagePanel swapPanel = createUsagePanel(20);
        content.getChildren().add(swapPanel.container());

        // Runtime memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.runtime")));
        MemoryUsagePanel runtimePanel = createUsagePanel(20);
        content.getChildren().add(runtimePanel.container());
        GridPane runtimeGrid = createInfoGrid();
        content.getChildren().add(runtimeGrid);

        // Physical memory sticks
        GlobalMemory memory = systemInfoService.getMemory();
        List<PhysicalMemory> physMems = memory.getPhysicalMemory();
        if (!physMems.isEmpty()) {
            content.getChildren().add(createSectionLabel(I18N.get("memory.physicalInfo")));
            for (PhysicalMemory pm : physMems) {
                GridPane pmGrid = createInfoGrid();
                int row = 0;
                addGridRow(pmGrid, row++, I18N.get("memory.bankLabel"), pm.getBankLabel());
                addGridRow(pmGrid, row++, I18N.get("detail.manufacturer"), pm.getManufacturer());
                addGridRow(pmGrid, row++, I18N.get("memory.capacity"),
                    SystemInfoService.formatBytes(pm.getCapacity()));
                addGridRow(pmGrid, row++, I18N.get("memory.memoryType"), pm.getMemoryType());
                addGridRow(pmGrid, row++, I18N.get("memory.clockSpeed"),
                    pm.getClockSpeed() > 0 ? String.format("%.0f MHz", pm.getClockSpeed() / 1_000_000.0) : "N/A");
                content.getChildren().add(pmGrid);
            }
        }

        // Schedule memory usage updates
        scheduler.scheduleAtFixedRate(() -> {
            GlobalMemory mem = systemInfoService.getMemory();
            long total = mem.getTotal();
            long available = mem.getAvailable();
            long used = Math.max(total - available, 0);
            long physicalRemain = Math.max(total - used, 0);
            VirtualMemory vm = mem.getVirtualMemory();

            long virtualTotal = normalizedTotal(vm.getVirtualMax(), vm.getVirtualInUse());
            long virtualUsed = Math.min(Math.max(vm.getVirtualInUse(), 0), virtualTotal);
            long virtualRemain = Math.max(virtualTotal - virtualUsed, 0);

            long swapTotal = normalizedTotal(vm.getSwapTotal(), vm.getSwapUsed());
            long swapUsed = Math.min(Math.max(vm.getSwapUsed(), 0), swapTotal);
            long swapRemain = Math.max(swapTotal - swapUsed, 0);

            Runtime runtime = Runtime.getRuntime();
            long runtimeTotal = runtime.totalMemory();
            long runtimeMax = runtime.maxMemory() > 0 ? runtime.maxMemory() : runtimeTotal;
            long runtimeUsed = Math.min(Math.max(runtimeTotal - runtime.freeMemory(), 0), runtimeMax);
            long runtimeRemain = Math.max(runtimeMax - runtimeUsed, 0);
            Platform.runLater(() -> {
                updateUsagePanel(physicalPanel, used, total, physicalRemain, "memory.usage.available");
                updateUsagePanel(virtualPanel, virtualUsed, virtualTotal, virtualRemain,
                    "memory.usage.available");
                updateUsagePanel(swapPanel, swapUsed, swapTotal, swapRemain, "memory.usage.available");
                updateUsagePanel(runtimePanel, runtimeUsed, runtimeMax, runtimeRemain,
                    "memory.usage.free");

                int row = 0;
                runtimeGrid.getChildren().clear();
                addGridRow(runtimeGrid, row++, I18N.get("memory.runtime.max"),
                    SystemInfoService.formatBytes(runtimeMax));
                addGridRow(runtimeGrid, row++, I18N.get("memory.runtime.allocated"),
                    SystemInfoService.formatBytes(runtimeTotal));
                addGridRow(runtimeGrid, row++, I18N.get("memory.runtime.used"),
                    SystemInfoService.formatBytes(runtimeUsed));
                addGridRow(runtimeGrid, row, I18N.get("memory.runtime.free"),
                    SystemInfoService.formatBytes(Math.max(runtimeTotal - runtimeUsed, 0)));
            });
        }, 0, 3, TimeUnit.SECONDS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    static double calculateUsage(long used, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.min(Math.max((double) used / total, 0), 1.0);
    }

    static String formatPercentText(double usage) {
        return String.format("%.0f%%", usage * 100);
    }

    private static long normalizedTotal(long total, long used) {
        if (total > 0) {
            return total;
        }
        return Math.max(used, 0);
    }

    private void updateUsagePanel(MemoryUsagePanel panel, long used, long total, long remain, String remainKey) {
        double usage = calculateUsage(used, total);
        panel.progressBar().setProgress(usage);
        panel.percentLabel().setText(formatPercentText(usage));
        panel.usedLabel().setText(I18N.get("memory.usage.used",
            SystemInfoService.formatBytes(used),
            SystemInfoService.formatBytes(total)));
        panel.remainLabel().setText(I18N.get(remainKey,
            SystemInfoService.formatBytes(Math.max(remain, 0)),
            SystemInfoService.formatBytes(total)));
    }

    private MemoryUsagePanel createUsagePanel(double barHeight) {
        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(barHeight);

        Label percentLabel = new Label("0%");
        percentLabel.getStyleClass().add("key-label");

        StackPane barPane = new StackPane(bar, percentLabel);
        barPane.setAlignment(Pos.CENTER);
        barPane.setMaxWidth(Double.MAX_VALUE);

        Label usedLabel = new Label();
        Label remainLabel = new Label();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox infoRow = new HBox(8, usedLabel, spacer, remainLabel);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(4, barPane, infoRow);
        container.setFillWidth(true);
        return new MemoryUsagePanel(container, bar, percentLabel, usedLabel, remainLabel);
    }

    /**
     * UI elements for a memory usage panel.
     */
    private record MemoryUsagePanel(
        VBox container,
        ProgressBar progressBar,
        Label percentLabel,
        Label usedLabel,
        Label remainLabel
    ) {
    }
}
