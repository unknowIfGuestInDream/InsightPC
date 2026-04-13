package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Sensors;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Builds the CPU tab showing processor information, live usage bar,
 * per-core usage bars, and usage area chart.
 */
public class CpuTabBuilder extends AbstractTabBuilder {

    private static final int MAX_DATA_POINTS = 30;
    private static final double CORE_NAME_MIN_WIDTH = 55;
    private static final double SYSTEM_BAR_HEIGHT = 36;
    private static final double CORE_BAR_HEIGHT = 18;
    private static final double METRIC_TITLE_FONT_SIZE = 13;
    private static final double METRIC_VALUE_FONT_SIZE = 28;
    private static final double INFO_GRID_KEY_MIN_WIDTH = 130;
    private static final double INFO_GRID_KEY_PREF_WIDTH = 140;
    private static final String PROGRESS_PERCENT_TEXT_STYLE = "-fx-text-fill: black;";
    private static final String READONLY_VALUE_FIELD_STYLE =
        "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;";
    static final double DEFAULT_DIVIDER_POSITION = 0.68;

    public CpuTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.cpu"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignC.CPU_64_BIT));

        VBox leftContent = new VBox(10);
        leftContent.setPadding(new Insets(15));
        VBox rightContent = new VBox(10);
        rightContent.setPadding(new Insets(15));

        CentralProcessor cpu = systemInfoService.getProcessor();
        Sensors sensors = systemInfoService.getSensors();
        CentralProcessor.ProcessorIdentifier id = cpu.getProcessorIdentifier();

        rightContent.getChildren().add(createSectionLabel(I18N.get("cpu.info")));
        GridPane metricsGrid = createMetricsGrid();
        Label usageMetricValue = createMetricValueLabel(formatSystemUsageText(0));
        Label unusedMetricValue = createMetricValueLabel(formatSystemUsageText(1));
        Label freqMetricValue = createMetricValueLabel(formatFrequencyText(cpu.getCurrentFreq(), cpu.getMaxFreq()));
        Label interruptsMetricValue = createMetricValueLabel(formatCounterText(cpu.getInterrupts()));
        Label contextSwitchesMetricValue = createMetricValueLabel(formatCounterText(cpu.getContextSwitches()));
        Label temperatureMetricValue = createMetricValueLabel(formatTemperatureText(sensors.getCpuTemperature()));
        Label voltageMetricValue = createMetricValueLabel(formatVoltageText(sensors.getCpuVoltage()));
        Label fanSpeedsMetricValue = createMetricValueLabel(formatFanSpeedsText(sensors.getFanSpeeds()));
        int metricRow = 0;
        metricsGrid.add(createMetricCard(I18N.get("cpu.inUse"), usageMetricValue), 0, metricRow);
        metricsGrid.add(createMetricCard(I18N.get("cpu.unused"), unusedMetricValue), 1, metricRow);
        metricsGrid.add(createMetricCard(I18N.get("cpu.freq"), freqMetricValue), 2, metricRow++);
        metricsGrid.add(createMetricsSeparator(), 0, metricRow++, 3, 1);
        metricsGrid.add(createMetricCard(I18N.get("cpu.interrupts"), interruptsMetricValue), 0, metricRow);
        metricsGrid.add(createMetricCard(I18N.get("cpu.contextSwitches"), contextSwitchesMetricValue), 1, metricRow++);
        metricsGrid.add(createMetricsSeparator(), 0, metricRow++, 3, 1);
        metricsGrid.add(createMetricCard(I18N.get("cpu.temperature"), temperatureMetricValue), 0, metricRow);
        metricsGrid.add(createMetricCard(I18N.get("cpu.voltage"), voltageMetricValue), 1, metricRow);
        metricsGrid.add(createMetricCard(I18N.get("cpu.fanSpeeds"), fanSpeedsMetricValue), 2, metricRow);
        rightContent.getChildren().add(metricsGrid);

        GridPane grid = createProcessorInfoGrid();
        int row = 0;
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.name"), id.getName());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.vendor"), id.getVendor());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.family"), id.getFamily());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.model"), id.getModel());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.stepping"), id.getStepping());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.identifier"), id.getIdentifier());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.microarchitecture"), id.getMicroarchitecture());
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.physicalCores"),
            String.valueOf(cpu.getPhysicalProcessorCount()));
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.logicalCores"),
            String.valueOf(cpu.getLogicalProcessorCount()));
        addReadOnlyValueRow(grid, row++, I18N.get("cpu.maxFreq"),
            String.format("%.2f GHz", cpu.getMaxFreq() / 1_000_000_000.0));
        rightContent.getChildren().add(grid);

        // CPU usage area chart
        NumberAxis xAxis = new NumberAxis(0, MAX_DATA_POINTS - 1, 1);
        xAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("%");
        yAxis.setAnimated(false);

        AreaChart<Number, Number> cpuChart = new AreaChart<>(xAxis, yAxis);
        cpuChart.setTitle(I18N.get("cpu.usage"));
        cpuChart.setAnimated(false);
        cpuChart.setCreateSymbols(false);
        cpuChart.setLegendVisible(false);
        cpuChart.setHorizontalGridLinesVisible(false);
        cpuChart.setVerticalGridLinesVisible(false);
        cpuChart.setAlternativeRowFillVisible(false);
        cpuChart.setAlternativeColumnFillVisible(false);
        cpuChart.setPrefHeight(280);
        VBox.setVgrow(cpuChart, Priority.ALWAYS);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(I18N.get("cpu.usage"));
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            series.getData().add(new XYChart.Data<>(i, 0));
        }
        cpuChart.setData(FXCollections.observableArrayList(series));
        leftContent.getChildren().add(cpuChart);

        // Overall CPU usage progress bar
        leftContent.getChildren().add(createSectionLabel(I18N.get("cpu.systemUsage")));
        ProgressBar cpuBar = new ProgressBar(0);
        cpuBar.setMaxWidth(Double.MAX_VALUE);
        cpuBar.setPrefHeight(SYSTEM_BAR_HEIGHT);
        Label cpuUsageLabel = new Label("0%");
        cpuUsageLabel.setAlignment(Pos.CENTER);
        cpuUsageLabel.setStyle(PROGRESS_PERCENT_TEXT_STYLE);

        StackPane usageBarPane = new StackPane(cpuBar, cpuUsageLabel);
        HBox usageBox = new HBox(usageBarPane);
        usageBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(usageBarPane, Priority.ALWAYS);
        leftContent.getChildren().add(usageBox);

        // Per-core CPU usage bars
        int logicalCores = cpu.getLogicalProcessorCount();
        leftContent.getChildren().add(createSectionLabel(I18N.get("cpu.processorUsage")));
        VBox coreBoxContainer = new VBox(6);
        coreBoxContainer.setPadding(new Insets(5, 0, 5, 0));

        ProgressBar[] coreBars = new ProgressBar[logicalCores];
        Label[] coreLabels = new Label[logicalCores];
        for (int i = 0; i < logicalCores; i++) {
            Label coreName = new Label(I18N.get("cpu.core") + " " + i);
            coreName.getStyleClass().add("key-label");
            coreName.setMinWidth(CORE_NAME_MIN_WIDTH);
            ProgressBar coreBar = new ProgressBar(0);
            coreBar.setMaxWidth(Double.MAX_VALUE);
            coreBar.setPrefHeight(CORE_BAR_HEIGHT);
            Label corePercent = new Label("0%");
            corePercent.setStyle(PROGRESS_PERCENT_TEXT_STYLE);
            StackPane coreBarPane = new StackPane(coreBar, corePercent);
            HBox coreRow = new HBox(8, coreName, coreBarPane);
            coreRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(coreBarPane, Priority.ALWAYS);
            coreBars[i] = coreBar;
            coreLabels[i] = corePercent;
            coreBoxContainer.getChildren().add(coreRow);
        }
        leftContent.getChildren().add(coreBoxContainer);

        // Schedule CPU usage updates
        final long[][] prevTicksHolder = {cpu.getSystemCpuLoadTicks()};
        final long[][][] prevCoreTicks = {cpu.getProcessorCpuLoadTicks()};
        scheduler.scheduleAtFixedRate(() -> {
            double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicksHolder[0]);
            prevTicksHolder[0] = cpu.getSystemCpuLoadTicks();
            double[] coreLoads = cpu.getProcessorCpuLoadBetweenTicks(prevCoreTicks[0]);
            prevCoreTicks[0] = cpu.getProcessorCpuLoadTicks();
            Platform.runLater(() -> {
                double normalizedCpuLoad = normalizeLoad(cpuLoad);
                cpuBar.setProgress(normalizedCpuLoad);
                cpuUsageLabel.setText(formatSystemUsageText(normalizedCpuLoad));
                usageMetricValue.setText(formatSystemUsageText(normalizedCpuLoad));
                unusedMetricValue.setText(formatSystemUsageText(calculateUnusedLoad(normalizedCpuLoad)));
                freqMetricValue.setText(formatFrequencyText(cpu.getCurrentFreq(), cpu.getMaxFreq()));
                interruptsMetricValue.setText(formatCounterText(cpu.getInterrupts()));
                contextSwitchesMetricValue.setText(formatCounterText(cpu.getContextSwitches()));
                temperatureMetricValue.setText(formatTemperatureText(sensors.getCpuTemperature()));
                voltageMetricValue.setText(formatVoltageText(sensors.getCpuVoltage()));
                fanSpeedsMetricValue.setText(formatFanSpeedsText(sensors.getFanSpeeds()));

                int updateCount = Math.min(coreLoads.length, coreBars.length);
                for (int i = 0; i < updateCount; i++) {
                    double normalizedCoreLoad = normalizeLoad(coreLoads[i]);
                    coreBars[i].setProgress(normalizedCoreLoad);
                    coreLabels[i].setText(formatCoreUsageText(normalizedCoreLoad));
                }

                int dataSize = series.getData().size();
                for (int i = 1; i < dataSize; i++) {
                    series.getData().get(i - 1).setYValue(series.getData().get(i).getYValue());
                }
                series.getData().get(dataSize - 1).setYValue(normalizedCpuLoad * 100);
            });
        }, 1, 2, TimeUnit.SECONDS);

        ScrollPane leftScrollPane = new ScrollPane(leftContent);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);

        ScrollPane rightScrollPane = new ScrollPane(rightContent);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setFitToHeight(true);

        SplitPane splitPane = new SplitPane(leftScrollPane, rightScrollPane);
        splitPane.setDividerPositions(DEFAULT_DIVIDER_POSITION);
        tab.setContent(splitPane);
        return tab;
    }

    static double normalizeLoad(double load) {
        if (!Double.isFinite(load) || load < 0) {
            return 0;
        }
        return Math.min(load, 1.0);
    }

    static String formatSystemUsageText(double normalizedLoad) {
        return String.format("%.1f%%", normalizedLoad * 100);
    }

    static String formatCoreUsageText(double normalizedLoad) {
        return String.format("%.2f%%", normalizedLoad * 100);
    }

    static double calculateUnusedLoad(double normalizedUsedLoad) {
        return 1 - normalizedUsedLoad;
    }

    static String formatFrequencyText(long[] currentFrequencies, long maxFrequency) {
        if (currentFrequencies != null && currentFrequencies.length > 0) {
            long total = 0;
            int count = 0;
            for (long currentFrequency : currentFrequencies) {
                if (currentFrequency > 0) {
                    total += currentFrequency;
                    count++;
                }
            }
            if (count > 0) {
                return String.format("%.2f", (double) total / count / 1_000_000_000.0);
            }
        }
        if (maxFrequency > 0) {
            return String.format("%.2f", maxFrequency / 1_000_000_000.0);
        }
        return "N/A";
    }

    static String formatCounterText(long value) {
        return value >= 0 ? Long.toString(value) : "N/A";
    }

    static String formatTemperatureText(double temperature) {
        return String.format("%.1f°C", Math.max(0, temperature));
    }

    static String formatVoltageText(double voltage) {
        return String.format("%.1f", Math.max(0, voltage));
    }

    static String formatFanSpeedsText(int[] fanSpeeds) {
        return fanSpeeds == null ? "[]" : Arrays.toString(fanSpeeds);
    }

    private GridPane createProcessorInfoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 0, 5, 10));
        ColumnConstraints keyCol = new ColumnConstraints();
        keyCol.setMinWidth(INFO_GRID_KEY_MIN_WIDTH);
        keyCol.setPrefWidth(INFO_GRID_KEY_PREF_WIDTH);
        ColumnConstraints valCol = new ColumnConstraints();
        valCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(keyCol, valCol);
        return grid;
    }

    private TextField addReadOnlyValueRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.getStyleClass().add("key-label");
        TextField valueField = new TextField(value != null ? value : "N/A");
        valueField.setEditable(false);
        valueField.setFocusTraversable(false);
        valueField.setAccessibleText(key);
        valueField.setStyle(READONLY_VALUE_FIELD_STYLE);
        keyLabel.setLabelFor(valueField);
        grid.add(keyLabel, 0, row);
        grid.add(valueField, 1, row);
        return valueField;
    }

    private GridPane createMetricsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 8, 0));
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(33.3);
            grid.getColumnConstraints().add(col);
        }
        return grid;
    }

    private VBox createMetricCard(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: " + METRIC_TITLE_FONT_SIZE + ";");
        VBox card = new VBox(2, titleLabel, valueLabel);
        card.setFillWidth(true);
        card.setPadding(new Insets(4, 0, 6, 0));
        return card;
    }

    private Label createMetricValueLabel(String initialValue) {
        Label valueLabel = new Label(initialValue);
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        valueLabel.setStyle("-fx-font-size: " + METRIC_VALUE_FONT_SIZE + "; -fx-font-weight: bold;");
        return valueLabel;
    }

    private Separator createMetricsSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(4, 0, 4, 0));
        return separator;
    }
}
