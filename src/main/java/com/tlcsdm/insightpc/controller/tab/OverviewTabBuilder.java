package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import oshi.hardware.*;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Builds the Overview tab showing a hardware summary with a memory pie chart.
 */
public class OverviewTabBuilder extends AbstractTabBuilder {

    public OverviewTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.overview"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignD.DESKTOP_TOWER_MONITOR));

        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));

        CentralProcessor cpu = systemInfoService.getProcessor();
        GlobalMemory memory = systemInfoService.getMemory();
        ComputerSystem cs = systemInfoService.getComputerSystem();
        Baseboard baseboard = cs.getBaseboard();
        Firmware firmware = cs.getFirmware();
        List<GraphicsCard> graphicsCards = systemInfoService.getGraphicsCards();
        List<HWDiskStore> diskStores = systemInfoService.getDiskStores();
        List<Display> displays = systemInfoService.getDisplays();
        List<SoundCard> soundCards = systemInfoService.getSoundCards();
        List<PowerSource> powerSources = systemInfoService.getPowerSources();

        // CPU
        infoBox.getChildren().add(createOverviewRow(MaterialDesignD.DESKTOP_CLASSIC,
            I18N.get("overview.cpu.label"),
            cpu.getProcessorIdentifier().getName()));

        // Memory
        List<PhysicalMemory> physMems = memory.getPhysicalMemory();
        String memoryInfo;
        if (!physMems.isEmpty()) {
            memoryInfo = SystemInfoService.formatBytes(memory.getTotal()) + " ("
                + physMems.stream()
                .map(pm -> SystemInfoService.formatBytes(pm.getCapacity())
                    + " " + pm.getMemoryType()
                    + (pm.getClockSpeed() > 0 ? " " + (pm.getClockSpeed() / 1_000_000) + "MHz" : ""))
                .collect(Collectors.joining(" + "))
                + ")";
        } else {
            memoryInfo = SystemInfoService.formatBytes(memory.getTotal());
        }
        infoBox.getChildren().add(createOverviewRow(MaterialDesignM.MEMORY,
            I18N.get("overview.memory.label"), memoryInfo));

        // Graphics Card
        String gpuInfo = graphicsCards.isEmpty() ? "N/A"
            : graphicsCards.stream()
            .map(gc -> gc.getName()
                + (gc.getVRam() > 0 ? " " + SystemInfoService.formatBytes(gc.getVRam()) : ""))
            .collect(Collectors.joining(", "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignH.HDMI_PORT,
            I18N.get("overview.graphicsCard"), gpuInfo));

        // BaseBoard
        String baseboardInfo = baseboard.getManufacturer() + " " + baseboard.getModel()
            + " " + baseboard.getVersion();
        infoBox.getChildren().add(createOverviewRow(MaterialDesignD.DEVELOPER_BOARD,
            I18N.get("overview.baseboard"), baseboardInfo.trim()));

        // Disk Storage
        String diskInfo = diskStores.isEmpty() ? "N/A"
            : diskStores.stream()
            .map(d -> d.getModel().trim() + " " + SystemInfoService.formatBytes(d.getSize()))
            .collect(Collectors.joining(" + "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignH.HARDDISK,
            I18N.get("overview.diskStorage"), diskInfo));

        // Display
        String displayInfo = displays.isEmpty() ? "N/A"
            : displays.size() + " " + I18N.get("overview.displaysConnected");
        infoBox.getChildren().add(createOverviewRow(MaterialDesignM.MONITOR,
            I18N.get("overview.display"), displayInfo));

        // Sound Card
        String soundInfo = soundCards.isEmpty() ? "N/A"
            : soundCards.stream()
            .map(SoundCard::getName)
            .collect(Collectors.joining(", "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignV.VOLUME_HIGH,
            I18N.get("overview.soundCard"), soundInfo));

        // Power Source
        String powerInfo = powerSources.isEmpty() ? "N/A"
            : powerSources.stream()
            .map(ps -> ps.getName() + " " + ps.getDeviceName()
                + " " + ps.getCurrentCapacity() + "/" + ps.getMaxCapacity()
                + " (" + ps.getChemistry() + ")")
            .collect(Collectors.joining(", "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignB.BATTERY,
            I18N.get("overview.powerSource"), powerInfo));

        // Firmware
        String firmwareInfo = firmware.getManufacturer() + " " + firmware.getName()
            + " " + firmware.getVersion() + " " + firmware.getReleaseDate();
        infoBox.getChildren().add(createOverviewRow(MaterialDesignS.SHIELD_CHECK,
            I18N.get("overview.firmware"), firmwareInfo.trim()));

        // Memory PieChart
        long usedMem = memory.getTotal() - memory.getAvailable();
        long availMem = memory.getAvailable();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data(I18N.get("memory.used") + " " + SystemInfoService.formatBytes(usedMem), usedMem),
            new PieChart.Data(I18N.get("memory.available") + " " + SystemInfoService.formatBytes(availMem), availMem)
        );
        PieChart memoryChart = new PieChart(pieData);
        memoryChart.setTitle(I18N.get("overview.memory"));
        memoryChart.setLabelsVisible(true);
        memoryChart.setLegendVisible(true);
        memoryChart.setPrefHeight(250);
        memoryChart.setMaxHeight(250);

        VBox chartBox = new VBox(memoryChart);
        chartBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, infoBox, chartBox);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
