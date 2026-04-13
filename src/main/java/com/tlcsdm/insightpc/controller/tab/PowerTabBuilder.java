package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import oshi.hardware.PowerSource;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Power tab showing battery/power source info.
 */
public class PowerTabBuilder extends AbstractTabBuilder {

    public PowerTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.power"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignB.BATTERY));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<PowerSource> powerSources = systemInfoService.getPowerSources();

        content.getChildren().add(createSectionLabel(I18N.get("power.info")));

        if (powerSources.isEmpty()) {
            content.getChildren().add(new Label(I18N.get("power.unknown")));
        } else {
            String na = I18N.get("power.notAvailable");
            for (PowerSource ps : powerSources) {
                ps.updateAttributes();
                content.getChildren().add(createSectionLabel(buildPowerHeader(ps, na)));

                double remainingRatio = normalizeUsage(ps.getRemainingCapacityPercent());
                ProgressBar capacityBar = new ProgressBar(remainingRatio);
                capacityBar.setMaxWidth(Double.MAX_VALUE);
                capacityBar.setPrefHeight(20);
                Label percentLabel = new Label(formatPercentText(remainingRatio));
                percentLabel.getStyleClass().add("usage-percent-label");
                StackPane barPane = new StackPane(capacityBar, percentLabel);
                barPane.setAlignment(Pos.CENTER);
                barPane.setMaxWidth(Double.MAX_VALUE);

                Label usageLabel = new Label(formatUsageInfo(ps, na));
                Label statusLabel = new Label(formatStatusInfo(ps, na));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox summaryRow = new HBox(8, usageLabel, spacer, statusLabel);
                summaryRow.setAlignment(Pos.CENTER_LEFT);
                VBox usagePanel = new VBox(4, barPane, summaryRow);
                usagePanel.setFillWidth(true);
                content.getChildren().add(usagePanel);

                GridPane grid = createInfoGrid();
                int row = 0;
                addGridRow(grid, row++, I18N.get("power.name"), ps.getName());
                addGridRow(grid, row++, I18N.get("power.deviceName"), ps.getDeviceName());
                addGridRow(grid, row++, I18N.get("power.remainingCapacityPercent"),
                    ps.getRemainingCapacityPercent() >= 0
                        ? String.format("%.1f%%", ps.getRemainingCapacityPercent() * 100) : na);

                double timeRemaining = ps.getTimeRemainingEstimated();
                String timeStr;
                if (timeRemaining < -1) {
                    timeStr = I18N.get("power.unlimited");
                } else if (timeRemaining < 0) {
                    timeStr = ps.isPowerOnLine() ? I18N.get("power.unlimited") : I18N.get("power.calculating");
                } else {
                    timeStr = SystemInfoService.formatUptime((long) timeRemaining);
                }
                addGridRow(grid, row++, I18N.get("power.timeRemainingEstimated"), timeStr);

                addGridRow(grid, row++, I18N.get("power.voltage"),
                    ps.getVoltage() >= 0 ? String.format("%.1f V", ps.getVoltage()) : na);
                addGridRow(grid, row++, I18N.get("power.amperage"),
                    String.format("%.1f mA", ps.getAmperage()));
                addGridRow(grid, row++, I18N.get("power.powerUsageRate"),
                    String.format("%.1f mW", ps.getPowerUsageRate()));
                addGridRow(grid, row++, I18N.get("power.powerOnLine"),
                    String.valueOf(ps.isPowerOnLine()));
                addGridRow(grid, row++, I18N.get("power.charging"),
                    String.valueOf(ps.isCharging()));
                addGridRow(grid, row++, I18N.get("power.discharging"),
                    String.valueOf(ps.isDischarging()));
                addGridRow(grid, row++, I18N.get("power.currentCapacity"),
                    ps.getCurrentCapacity() > 0 ? String.valueOf(ps.getCurrentCapacity()) : na);
                addGridRow(grid, row++, I18N.get("power.maxCapacity"),
                    ps.getMaxCapacity() > 0 ? String.valueOf(ps.getMaxCapacity()) : na);
                addGridRow(grid, row++, I18N.get("power.designCapacity"),
                    ps.getDesignCapacity() > 0 ? String.valueOf(ps.getDesignCapacity()) : na);
                addGridRow(grid, row++, I18N.get("power.cycleCount"),
                    ps.getCycleCount() >= 0 ? String.valueOf(ps.getCycleCount()) : na);
                addGridRow(grid, row++, I18N.get("power.chemistry"), ps.getChemistry());
                addGridRow(grid, row++, I18N.get("power.manufacturer"), ps.getManufacturer());
                addGridRow(grid, row++, I18N.get("power.serialNumber"), ps.getSerialNumber());
                addGridRow(grid, row++, I18N.get("power.temperature"),
                    ps.getTemperature() > 0 ? String.format("%.1f °C", ps.getTemperature()) : na);

                content.getChildren().add(grid);
                content.getChildren().add(new Separator());
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    static double normalizeUsage(double usage) {
        if (Double.isNaN(usage) || Double.isInfinite(usage)) {
            return 0.0;
        }
        return Math.min(Math.max(usage, 0), 1.0);
    }

    static String formatPercentText(double usage) {
        return String.format("%.0f%%", normalizeUsage(usage) * 100);
    }

    static String normalizeFieldValue(String value) {
        if (value == null) {
            return I18N.get("power.notAvailable");
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? I18N.get("power.notAvailable") : trimmed;
    }

    private String formatUsageInfo(PowerSource ps, String na) {
        String current = ps.getCurrentCapacity() > 0 ? String.valueOf(ps.getCurrentCapacity()) : na;
        String max = ps.getMaxCapacity() > 0 ? String.valueOf(ps.getMaxCapacity()) : na;
        String design = ps.getDesignCapacity() > 0 ? String.valueOf(ps.getDesignCapacity()) : na;
        if (na.equals(current) && na.equals(max) && na.equals(design)) {
            return na;
        }
        return String.format("%s %s / %s %s / %s %s",
            I18N.get("power.currentCapacity"), current,
            I18N.get("power.maxCapacity"), max,
            I18N.get("power.designCapacity"), design);
    }

    private String buildPowerHeader(PowerSource ps, String na) {
        String name = normalizeFieldValue(ps.getName());
        String deviceName = normalizeFieldValue(ps.getDeviceName());
        String chemistry = normalizeFieldValue(ps.getChemistry());
        if (na.equals(deviceName) && na.equals(chemistry)) {
            return name;
        }
        if (na.equals(chemistry)) {
            return String.format("%s - %s", name, deviceName);
        }
        if (na.equals(deviceName)) {
            return String.format("%s - %s", name, chemistry);
        }
        return String.format("%s - %s (%s)", name, deviceName, chemistry);
    }

    private String formatStatusInfo(PowerSource ps, String na) {
        String timeStr = resolveTimeRemainingText(ps);
        String temp = ps.getTemperature() > 0 ? String.format("%.1f°C", ps.getTemperature()) : na;
        return I18N.get("power.timeRemainingEstimated") + ": " + timeStr + " / " + temp;
    }

    private String resolveTimeRemainingText(PowerSource ps) {
        double timeRemaining = ps.getTimeRemainingEstimated();
        if (timeRemaining < -1) {
            return I18N.get("power.unlimited");
        }
        if (timeRemaining < 0) {
            return ps.isPowerOnLine() ? I18N.get("power.unlimited") : I18N.get("power.calculating");
        }
        return SystemInfoService.formatUptime((long) timeRemaining);
    }

    @Override
    protected void addGridRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.getStyleClass().add("key-label");

        TextField valueField = new TextField(normalizeFieldValue(value));
        valueField.setEditable(false);
        valueField.getStyleClass().add("detail-value-field");
        GridPane.setHgrow(valueField, Priority.ALWAYS);

        grid.add(keyLabel, 0, row);
        grid.add(valueField, 1, row);
    }
}
