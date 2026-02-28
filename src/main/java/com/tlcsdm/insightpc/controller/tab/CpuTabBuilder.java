package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import oshi.hardware.CentralProcessor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Builds the CPU tab showing processor information, live usage bar,
 * per-core usage bars, and usage area chart.
 */
public class CpuTabBuilder extends AbstractTabBuilder {

    private static final int MAX_DATA_POINTS = 30;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public CpuTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.cpu"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignC.CPU_64_BIT));

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

        // Overall CPU usage progress bar
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

        // Per-core CPU usage bars
        int logicalCores = cpu.getLogicalProcessorCount();
        content.getChildren().add(createSectionLabel(I18N.get("cpu.perCoreUsage")));
        int columns = Math.min(logicalCores, 4);
        GridPane coreGrid = new GridPane();
        coreGrid.setHgap(15);
        coreGrid.setVgap(8);
        coreGrid.setPadding(new Insets(5, 0, 5, 10));
        for (int c = 0; c < columns; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columns);
            cc.setHgrow(Priority.ALWAYS);
            coreGrid.getColumnConstraints().add(cc);
        }

        ProgressBar[] coreBars = new ProgressBar[logicalCores];
        Label[] coreLabels = new Label[logicalCores];
        for (int i = 0; i < logicalCores; i++) {
            VBox coreBox = new VBox(2);
            Label coreName = new Label(I18N.get("cpu.core") + " " + i);
            coreName.getStyleClass().add("key-label");
            ProgressBar coreBar = new ProgressBar(0);
            coreBar.setMaxWidth(Double.MAX_VALUE);
            coreBar.setPrefHeight(18);
            Label corePercent = new Label("0%");
            HBox coreRow = new HBox(5, coreBar, corePercent);
            coreRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(coreBar, Priority.ALWAYS);
            coreBox.getChildren().addAll(coreName, coreRow);
            coreBars[i] = coreBar;
            coreLabels[i] = corePercent;
            coreGrid.add(coreBox, i % columns, i / columns);
        }
        content.getChildren().add(coreGrid);

        // CPU usage area chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("");
        xAxis.setAnimated(false);

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("%");
        yAxis.setAnimated(false);

        AreaChart<String, Number> cpuChart = new AreaChart<>(xAxis, yAxis);
        cpuChart.setTitle(I18N.get("cpu.usage"));
        cpuChart.setAnimated(false);
        cpuChart.setCreateSymbols(false);
        cpuChart.setLegendVisible(false);
        cpuChart.setPrefHeight(300);
        VBox.setVgrow(cpuChart, Priority.ALWAYS);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(I18N.get("cpu.usage"));
        cpuChart.setData(FXCollections.observableArrayList(series));

        content.getChildren().add(cpuChart);

        // Schedule CPU usage updates
        final long[][] prevTicksHolder = {cpu.getSystemCpuLoadTicks()};
        final long[][][] prevCoreTicks = {cpu.getProcessorCpuLoadTicks()};
        scheduler.scheduleAtFixedRate(() -> {
            double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicksHolder[0]);
            prevTicksHolder[0] = cpu.getSystemCpuLoadTicks();
            double[] coreLoads = cpu.getProcessorCpuLoadBetweenTicks(prevCoreTicks[0]);
            prevCoreTicks[0] = cpu.getProcessorCpuLoadTicks();
            Platform.runLater(() -> {
                cpuBar.setProgress(cpuLoad);
                cpuUsageLabel.setText(String.format("%.1f%%", cpuLoad * 100));

                int updateCount = Math.min(coreLoads.length, coreBars.length);
                for (int i = 0; i < updateCount; i++) {
                    coreBars[i].setProgress(coreLoads[i]);
                    coreLabels[i].setText(String.format("%.0f%%", coreLoads[i] * 100));
                }

                String timeLabel = LocalTime.now().format(TIME_FMT);
                series.getData().add(new XYChart.Data<>(timeLabel, cpuLoad * 100));
                if (series.getData().size() > MAX_DATA_POINTS) {
                    series.getData().remove(0);
                }
            });
        }, 1, 2, TimeUnit.SECONDS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
