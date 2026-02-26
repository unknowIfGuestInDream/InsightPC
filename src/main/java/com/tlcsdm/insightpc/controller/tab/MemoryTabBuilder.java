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
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material.Material;
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
        tab.setGraphic(createTabIcon(Material.MEMORY));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        GlobalMemory memory = systemInfoService.getMemory();

        // Physical memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.physical")));
        GridPane grid = createInfoGrid();
        int row = 0;
        addGridRow(grid, row++, I18N.get("memory.total"),
            SystemInfoService.formatBytes(memory.getTotal()));
        addGridRow(grid, row++, I18N.get("memory.available"),
            SystemInfoService.formatBytes(memory.getAvailable()));
        addGridRow(grid, row++, I18N.get("memory.used"),
            SystemInfoService.formatBytes(memory.getTotal() - memory.getAvailable()));
        addGridRow(grid, row++, I18N.get("memory.pageSize"),
            SystemInfoService.formatBytes(memory.getPageSize()));
        content.getChildren().add(grid);

        // Memory usage bar
        ProgressBar memBar = new ProgressBar(0);
        memBar.setMaxWidth(Double.MAX_VALUE);
        memBar.setPrefHeight(25);
        Label memUsageLabel = new Label();
        HBox usageBox = new HBox(10, memBar, memUsageLabel);
        usageBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(memBar, Priority.ALWAYS);
        content.getChildren().add(usageBox);

        // Virtual memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.virtual")));
        VirtualMemory vm = memory.getVirtualMemory();
        GridPane vmGrid = createInfoGrid();
        row = 0;
        addGridRow(vmGrid, row++, I18N.get("memory.swapTotal"),
            SystemInfoService.formatBytes(vm.getSwapTotal()));
        addGridRow(vmGrid, row++, I18N.get("memory.swapUsed"),
            SystemInfoService.formatBytes(vm.getSwapUsed()));
        addGridRow(vmGrid, row++, I18N.get("memory.virtualMax"),
            SystemInfoService.formatBytes(vm.getVirtualMax()));
        addGridRow(vmGrid, row++, I18N.get("memory.virtualInUse"),
            SystemInfoService.formatBytes(vm.getVirtualInUse()));
        content.getChildren().add(vmGrid);

        // Physical memory sticks
        List<PhysicalMemory> physMems = memory.getPhysicalMemory();
        if (!physMems.isEmpty()) {
            content.getChildren().add(createSectionLabel(I18N.get("memory.sticks")));
            for (PhysicalMemory pm : physMems) {
                GridPane pmGrid = createInfoGrid();
                row = 0;
                addGridRow(pmGrid, row++, I18N.get("memory.bankLabel"), pm.getBankLabel());
                addGridRow(pmGrid, row++, I18N.get("memory.capacity"),
                    SystemInfoService.formatBytes(pm.getCapacity()));
                addGridRow(pmGrid, row++, I18N.get("memory.clockSpeed"),
                    String.format("%.0f MHz", pm.getClockSpeed() / 1_000_000.0));
                addGridRow(pmGrid, row++, I18N.get("memory.memoryType"), pm.getMemoryType());
                content.getChildren().add(pmGrid);
            }
        }

        // Schedule memory usage updates
        scheduler.scheduleAtFixedRate(() -> {
            GlobalMemory mem = systemInfoService.getMemory();
            long total = mem.getTotal();
            long available = mem.getAvailable();
            double usage = total > 0 ? (double) (total - available) / total : 0;
            Platform.runLater(() -> {
                memBar.setProgress(usage);
                memUsageLabel.setText(String.format("%.1f%% (%s / %s)",
                    usage * 100,
                    SystemInfoService.formatBytes(total - available),
                    SystemInfoService.formatBytes(total)));
            });
        }, 0, 3, TimeUnit.SECONDS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
