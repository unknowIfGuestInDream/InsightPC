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
import oshi.hardware.CentralProcessor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Builds the CPU tab showing processor information and live usage.
 */
public class CpuTabBuilder extends AbstractTabBuilder {

    public CpuTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.cpu"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.DEVELOPER_BOARD));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        CentralProcessor cpu = systemInfoService.getProcessor();
        CentralProcessor.ProcessorIdentifier id = cpu.getProcessorIdentifier();

        content.getChildren().add(createSectionLabel(I18N.get("cpu.info")));
        GridPane grid = createInfoGrid();
        int row = 0;
        addGridRow(grid, row++, I18N.get("cpu.name"), id.getName());
        addGridRow(grid, row++, I18N.get("cpu.vendor"), id.getVendor());
        addGridRow(grid, row++, I18N.get("cpu.family"), id.getFamily());
        addGridRow(grid, row++, I18N.get("cpu.model"), id.getModel());
        addGridRow(grid, row++, I18N.get("cpu.stepping"), id.getStepping());
        addGridRow(grid, row++, I18N.get("cpu.identifier"), id.getIdentifier());
        addGridRow(grid, row++, I18N.get("cpu.microarchitecture"), id.getMicroarchitecture());
        addGridRow(grid, row++, I18N.get("cpu.physicalCores"),
            String.valueOf(cpu.getPhysicalProcessorCount()));
        addGridRow(grid, row++, I18N.get("cpu.logicalCores"),
            String.valueOf(cpu.getLogicalProcessorCount()));
        addGridRow(grid, row++, I18N.get("cpu.maxFreq"),
            String.format("%.2f GHz", cpu.getMaxFreq() / 1_000_000_000.0));
        content.getChildren().add(grid);

        // CPU usage progress bar
        content.getChildren().add(createSectionLabel(I18N.get("cpu.usage")));
        ProgressBar cpuBar = new ProgressBar(0);
        cpuBar.setMaxWidth(Double.MAX_VALUE);
        cpuBar.setPrefHeight(25);
        Label cpuUsageLabel = new Label("0%");
        cpuUsageLabel.setAlignment(Pos.CENTER);

        HBox usageBox = new HBox(10, cpuBar, cpuUsageLabel);
        usageBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cpuBar, Priority.ALWAYS);
        content.getChildren().add(usageBox);

        // Schedule CPU usage updates
        final long[][] prevTicksHolder = {cpu.getSystemCpuLoadTicks()};
        scheduler.scheduleAtFixedRate(() -> {
            double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicksHolder[0]);
            prevTicksHolder[0] = cpu.getSystemCpuLoadTicks();
            Platform.runLater(() -> {
                cpuBar.setProgress(cpuLoad);
                cpuUsageLabel.setText(String.format("%.1f%%", cpuLoad * 100));
            });
        }, 1, 2, TimeUnit.SECONDS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
