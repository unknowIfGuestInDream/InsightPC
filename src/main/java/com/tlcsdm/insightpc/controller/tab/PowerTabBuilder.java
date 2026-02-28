package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
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
                    ps.getPowerUsageRate() >= 0 ? String.format("%.1f mW", ps.getPowerUsageRate()) : na);
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
}
