package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import oshi.hardware.*;
import oshi.util.EdidUtil;

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

        // Computer Model and OS Version header
        String computerModel = (cs.getManufacturer() + " " + cs.getModel()).trim();
        Label modelLabel = new Label(computerModel);
        modelLabel.getStyleClass().add("overview-model-label");

        String osInfo = systemInfoService.getOsManufacturer() + " "
            + systemInfoService.getOsFamily() + " "
            + systemInfoService.getOsVersionInfo();
        Label osLabel = new Label(osInfo.trim());
        osLabel.getStyleClass().add("overview-os-label");

        infoBox.getChildren().addAll(modelLabel, osLabel, new Separator());

        // CPU
        infoBox.getChildren().add(createOverviewRow(MaterialDesignC.CPU_64_BIT,
            I18N.get("overview.cpu.label"),
            cpu.getProcessorIdentifier().getName()));

        // Memory
        List<PhysicalMemory> physMems = memory.getPhysicalMemory();
        String memoryInfo;
        if (!physMems.isEmpty()) {
            memoryInfo = SystemInfoService.formatBytes(memory.getTotal()) + " ("
                + physMems.stream()
                .map(pm -> pm.getBankLabel() + " "
                    + SystemInfoService.formatBytes(pm.getCapacity())
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
            .collect(Collectors.joining(" + "));
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

        // Display - show model info from EDID
        String displayInfo = displays.isEmpty() ? "N/A"
            : displays.stream()
            .map(d -> {
                byte[] edid = d.getEdid();
                if (edid != null && edid.length >= 128) {
                    String mfgId = EdidUtil.getManufacturerID(edid);
                    String model = EdidUtil.getModel(edid);
                    String resolution = EdidUtil.getPreferredResolution(edid);
                    StringBuilder sb = new StringBuilder();
                    if (mfgId != null && !mfgId.isEmpty()) {
                        sb.append(mfgId);
                    }
                    if (model != null && !model.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(model);
                    }
                    if (resolution != null && !resolution.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(resolution);
                    }
                    if (sb.length() > 0) {
                        return sb.toString();
                    }
                }
                return I18N.get("overview.unknownDisplay");
            })
            .collect(Collectors.joining(" + "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignM.MONITOR,
            I18N.get("overview.display"), displayInfo));

        // Sound Card
        String soundInfo = soundCards.isEmpty() ? "N/A"
            : soundCards.stream()
            .map(SoundCard::getName)
            .collect(Collectors.joining(" + "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignV.VOLUME_HIGH,
            I18N.get("overview.soundCard"), soundInfo));

        // Power Source
        powerSources.forEach(PowerSource::updateAttributes);
        String powerInfo = powerSources.isEmpty() ? "N/A"
            : powerSources.stream()
            .map(ps -> {
                StringBuilder sb = new StringBuilder(ps.getName());
                double remaining = ps.getRemainingCapacityPercent();
                if (remaining >= 0) {
                    sb.append(" ").append(String.format("%.0f%%", remaining * 100));
                }
                if (ps.isPowerOnLine()) {
                    sb.append(" (").append(I18N.get("power.powerOnLine")).append(")");
                } else if (ps.isCharging()) {
                    sb.append(" (").append(I18N.get("power.charging")).append(")");
                } else if (ps.isDischarging()) {
                    sb.append(" (").append(I18N.get("power.discharging")).append(")");
                }
                return sb.toString();
            })
            .collect(Collectors.joining(", "));
        infoBox.getChildren().add(createOverviewRow(MaterialDesignB.BATTERY,
            I18N.get("overview.powerSource"), powerInfo));

        // Firmware
        String firmwareInfo = firmware.getManufacturer() + " " + firmware.getName()
            + " " + firmware.getVersion() + " " + firmware.getReleaseDate();
        infoBox.getChildren().add(createOverviewRow(MaterialDesignS.SHIELD_CHECK,
            I18N.get("overview.firmware"), firmwareInfo.trim()));

        VBox content = new VBox(10, infoBox);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
