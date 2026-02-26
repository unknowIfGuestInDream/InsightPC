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
import org.kordamp.ikonli.material.Material;
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
        tab.setGraphic(createTabIcon(Material.BATTERY_STD));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<PowerSource> powerSources = systemInfoService.getPowerSources();

        content.getChildren().add(createSectionLabel(I18N.get("power.info")));

        if (powerSources.isEmpty()) {
            content.getChildren().add(new Label(I18N.get("power.unknown")));
        } else {
            for (PowerSource ps : powerSources) {
                GridPane grid = createInfoGrid();
                int row = 0;
                addGridRow(grid, row++, I18N.get("power.name"), ps.getName());
                addGridRow(grid, row++, I18N.get("power.deviceName"), ps.getDeviceName());
                addGridRow(grid, row++, I18N.get("power.remainingCapacityPercent"),
                    String.format("%.1f%%", ps.getRemainingCapacityPercent() * 100));

                double timeRemaining = ps.getTimeRemainingEstimated();
                String timeStr;
                if (timeRemaining < 0) {
                    timeStr = ps.isPowerOnLine() ? I18N.get("power.unlimited") : I18N.get("power.calculating");
                } else {
                    timeStr = SystemInfoService.formatUptime((long) timeRemaining);
                }
                addGridRow(grid, row++, I18N.get("power.timeRemainingEstimated"), timeStr);

                addGridRow(grid, row++, I18N.get("power.voltage"),
                    String.format("%.1f V", ps.getVoltage()));
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
                    String.valueOf(ps.getCurrentCapacity()));
                addGridRow(grid, row++, I18N.get("power.maxCapacity"),
                    String.valueOf(ps.getMaxCapacity()));
                addGridRow(grid, row++, I18N.get("power.designCapacity"),
                    String.valueOf(ps.getDesignCapacity()));
                addGridRow(grid, row++, I18N.get("power.cycleCount"),
                    String.valueOf(ps.getCycleCount()));
                addGridRow(grid, row++, I18N.get("power.chemistry"), ps.getChemistry());
                addGridRow(grid, row++, I18N.get("power.manufacturer"), ps.getManufacturer());
                addGridRow(grid, row++, I18N.get("power.serialNumber"), ps.getSerialNumber());
                addGridRow(grid, row++, I18N.get("power.temperature"),
                    String.format("%.1f °C", ps.getTemperature()));

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
