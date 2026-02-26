package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material.Material;
import oshi.hardware.*;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Builds the Overview tab showing a hardware summary.
 */
public class OverviewTabBuilder extends AbstractTabBuilder {

    public OverviewTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.overview"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.DASHBOARD));

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

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
        content.getChildren().add(createOverviewRow(Material.DEVELOPER_BOARD,
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
        content.getChildren().add(createOverviewRow(Material.MEMORY,
            I18N.get("overview.memory.label"), memoryInfo));

        // Graphics Card
        String gpuInfo = graphicsCards.isEmpty() ? "N/A"
            : graphicsCards.stream()
            .map(gc -> gc.getName()
                + (gc.getVRam() > 0 ? " " + SystemInfoService.formatBytes(gc.getVRam()) : ""))
            .collect(Collectors.joining(", "));
        content.getChildren().add(createOverviewRow(Material.GRAPHIC_EQ,
            I18N.get("overview.graphicsCard"), gpuInfo));

        // BaseBoard
        String baseboardInfo = baseboard.getManufacturer() + " " + baseboard.getModel()
            + " " + baseboard.getVersion();
        content.getChildren().add(createOverviewRow(Material.DEVELOPER_BOARD,
            I18N.get("overview.baseboard"), baseboardInfo.trim()));

        // Disk Storage
        String diskInfo = diskStores.isEmpty() ? "N/A"
            : diskStores.stream()
            .map(d -> d.getModel().trim() + " " + SystemInfoService.formatBytes(d.getSize()))
            .collect(Collectors.joining(" + "));
        content.getChildren().add(createOverviewRow(Material.SD_STORAGE,
            I18N.get("overview.diskStorage"), diskInfo));

        // Display
        String displayInfo = displays.isEmpty() ? "N/A"
            : displays.size() + " " + I18N.get("overview.displaysConnected");
        content.getChildren().add(createOverviewRow(Material.DESKTOP_WINDOWS,
            I18N.get("overview.display"), displayInfo));

        // Sound Card
        String soundInfo = soundCards.isEmpty() ? "N/A"
            : soundCards.stream()
            .map(SoundCard::getName)
            .collect(Collectors.joining(", "));
        content.getChildren().add(createOverviewRow(Material.SPEAKER,
            I18N.get("overview.soundCard"), soundInfo));

        // Power Source
        String powerInfo = powerSources.isEmpty() ? "N/A"
            : powerSources.stream()
            .map(ps -> ps.getName() + " " + ps.getDeviceName()
                + " " + ps.getCurrentCapacity() + "/" + ps.getMaxCapacity()
                + " (" + ps.getChemistry() + ")")
            .collect(Collectors.joining(", "));
        content.getChildren().add(createOverviewRow(Material.BATTERY_STD,
            I18N.get("overview.powerSource"), powerInfo));

        // Firmware
        String firmwareInfo = firmware.getManufacturer() + " " + firmware.getName()
            + " " + firmware.getVersion() + " " + firmware.getReleaseDate();
        content.getChildren().add(createOverviewRow(Material.SECURITY,
            I18N.get("overview.firmware"), firmwareInfo.trim()));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
