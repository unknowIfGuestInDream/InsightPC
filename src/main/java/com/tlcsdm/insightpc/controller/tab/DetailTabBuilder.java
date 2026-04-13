package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.Firmware;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.NetworkIF;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.PowerSource;
import oshi.hardware.SoundCard;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;
import oshi.util.EdidUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Detail tab showing computer system, baseboard, and firmware info.
 */
public class DetailTabBuilder extends AbstractTabBuilder {

    private static final DateTimeFormatter BOOT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DetailTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.detail"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignI.INFORMATION_OUTLINE));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        OperatingSystem operatingSystem = systemInfoService.getOperatingSystem();
        ComputerSystem cs = systemInfoService.getComputerSystem();
        CentralProcessor cpu = systemInfoService.getProcessor();
        GlobalMemory memory = systemInfoService.getMemory();
        List<HWDiskStore> diskStores = systemInfoService.getDiskStores();
        List<GraphicsCard> graphicsCards = systemInfoService.getGraphicsCards();
        List<Display> displays = systemInfoService.getDisplays();
        List<SoundCard> soundCards = systemInfoService.getSoundCards();
        List<NetworkIF> networkIFs = systemInfoService.getNetworkInterfaces();
        List<PowerSource> powerSources = systemInfoService.getPowerSources();
        String na = I18N.get("power.notAvailable");

        // Operating System section
        content.getChildren().add(createSectionLabel(MaterialDesignI.INFORMATION_OUTLINE, I18N.get("detail.operatingSystem")));
        GridPane osGrid = createInfoGrid();
        int row = 0;
        OperatingSystem.OSVersionInfo versionInfo = operatingSystem.getVersionInfo();
        String version = versionInfo.getVersion() == null ? "" : versionInfo.getVersion();
        if (versionInfo.getCodeName() != null && !versionInfo.getCodeName().isBlank()) {
            version += " (" + versionInfo.getCodeName() + ")";
        }
        if (versionInfo.getBuildNumber() != null && !versionInfo.getBuildNumber().isBlank()) {
            version += " build " + versionInfo.getBuildNumber();
        }
        addGridRow(osGrid, row++, I18N.get("detail.manufacturer"), operatingSystem.getManufacturer());
        addGridRow(osGrid, row++, I18N.get("cpu.family"), operatingSystem.getFamily());
        addGridRow(osGrid, row++, I18N.get("detail.version"), version);
        addGridRow(osGrid, row++, I18N.get("detail.bitness"), String.valueOf(operatingSystem.getBitness()));
        addGridRow(osGrid, row++, I18N.get("detail.maxFileDescriptors"),
            String.valueOf(operatingSystem.getFileSystem().getMaxFileDescriptors()));
        addGridRow(osGrid, row++, I18N.get("detail.openFileDescriptors"),
            String.valueOf(operatingSystem.getFileSystem().getOpenFileDescriptors()));
        addGridRow(osGrid, row++, I18N.get("process.threadCount"), String.valueOf(operatingSystem.getThreadCount()));
        addGridRow(osGrid, row++, I18N.get("process.count"), String.valueOf(operatingSystem.getProcessCount()));
        addGridRow(osGrid, row++, I18N.get("detail.systemBootTime"),
            BOOT_TIME_FORMATTER.format(Instant.ofEpochSecond(operatingSystem.getSystemBootTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime()));
        addGridRow(osGrid, row++, I18N.get("detail.systemUptime"),
            SystemInfoService.formatUptime(operatingSystem.getSystemUptime()));
        content.getChildren().addAll(osGrid, new Separator());

        // Computer System section
        content.getChildren().add(createSectionLabel(MaterialDesignD.DESKTOP_TOWER_MONITOR, I18N.get("detail.computer")));
        GridPane csGrid = createInfoGrid();
        row = 0;
        addGridRow(csGrid, row++, I18N.get("detail.manufacturer"), cs.getManufacturer());
        addGridRow(csGrid, row++, I18N.get("detail.model"), cs.getModel());
        addGridRow(csGrid, row++, I18N.get("detail.serialNumber"), cs.getSerialNumber());
        addGridRow(csGrid, row++, I18N.get("detail.hardwareUUID"), cs.getHardwareUUID());
        Firmware firmware = cs.getFirmware();
        addGridRow(csGrid, row++, I18N.get("detail.firmwareManufacturer"), firmware.getManufacturer());
        addGridRow(csGrid, row++, I18N.get("detail.firmwareName"), firmware.getName());
        addGridRow(csGrid, row++, I18N.get("detail.firmwareDescription"), firmware.getDescription());
        addGridRow(csGrid, row++, I18N.get("detail.firmwareVersion"), firmware.getVersion());
        addGridRow(csGrid, row++, I18N.get("detail.firmwareReleaseDate"), firmware.getReleaseDate());
        content.getChildren().addAll(csGrid, new Separator());

        // Baseboard section
        Baseboard baseboard = cs.getBaseboard();
        content.getChildren().add(createSectionLabel(MaterialDesignD.DEVELOPER_BOARD, I18N.get("detail.baseboard")));
        GridPane bbGrid = createInfoGrid();
        row = 0;
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardManufacturer"), baseboard.getManufacturer());
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardModel"), baseboard.getModel());
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardVersion"), baseboard.getVersion());
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardSerialNumber"), baseboard.getSerialNumber());
        content.getChildren().addAll(bbGrid, new Separator());

        // CPU section
        content.getChildren().add(createSectionLabel(MaterialDesignC.CPU_64_BIT, I18N.get("tab.cpu")));
        CentralProcessor.ProcessorIdentifier processorIdentifier = cpu.getProcessorIdentifier();
        GridPane cpuGrid = createInfoGrid();
        row = 0;
        addGridRow(cpuGrid, row++, I18N.get("cpu.name"), processorIdentifier.getName());
        addGridRow(cpuGrid, row++, I18N.get("cpu.identifier"), processorIdentifier.getIdentifier());
        addGridRow(cpuGrid, row++, I18N.get("cpu.microarchitecture"), processorIdentifier.getMicroarchitecture());
        addGridRow(cpuGrid, row++, I18N.get("cpu.model"), processorIdentifier.getModel());
        addGridRow(cpuGrid, row++, I18N.get("cpu.family"), processorIdentifier.getFamily());
        addGridRow(cpuGrid, row++, I18N.get("detail.processorId"), processorIdentifier.getProcessorID());
        addGridRow(cpuGrid, row++, I18N.get("cpu.vendor"), processorIdentifier.getVendor());
        addGridRow(cpuGrid, row++, I18N.get("detail.vendorFreq"),
            processorIdentifier.getVendorFreq() > 0
                ? String.format("%.2f GHz", processorIdentifier.getVendorFreq() / 1_000_000_000.0)
                : na);
        addGridRow(cpuGrid, row++, I18N.get("cpu.stepping"), processorIdentifier.getStepping());
        addGridRow(cpuGrid, row++, I18N.get("detail.physicalPackageCount"), String.valueOf(cpu.getPhysicalPackageCount()));
        addGridRow(cpuGrid, row++, I18N.get("cpu.physicalCores"), String.valueOf(cpu.getPhysicalProcessorCount()));
        addGridRow(cpuGrid, row++, I18N.get("cpu.logicalCores"), String.valueOf(cpu.getLogicalProcessorCount()));
        addGridRow(cpuGrid, row++, I18N.get("cpu.maxFreq"),
            cpu.getMaxFreq() > 0 ? String.format("%.2f GHz", cpu.getMaxFreq() / 1_000_000_000.0) : na);
        content.getChildren().addAll(cpuGrid, new Separator());

        // Memory section
        content.getChildren().add(createSectionLabel(MaterialDesignM.MEMORY, I18N.get("tab.memory")));
        GridPane memoryGrid = createInfoGrid();
        row = 0;
        addGridRow(memoryGrid, row++, I18N.get("memory.total"), SystemInfoService.formatBytes(memory.getTotal()));
        addGridRow(memoryGrid, row++, I18N.get("memory.pageSize"), SystemInfoService.formatBytes(memory.getPageSize()));
        content.getChildren().add(memoryGrid);
        int index = 0;
        for (PhysicalMemory physicalMemory : memory.getPhysicalMemory()) {
            content.getChildren().add(createSectionLabel(I18N.get("detail.physicalMemory") + " #" + index++));
            GridPane physicalMemoryGrid = createInfoGrid();
            row = 0;
            addGridRow(physicalMemoryGrid, row++, I18N.get("memory.bankLabel"), physicalMemory.getBankLabel());
            addGridRow(physicalMemoryGrid, row++, I18N.get("detail.manufacturer"), physicalMemory.getManufacturer());
            addGridRow(physicalMemoryGrid, row++, I18N.get("memory.capacity"),
                SystemInfoService.formatBytes(physicalMemory.getCapacity()));
            addGridRow(physicalMemoryGrid, row++, I18N.get("memory.memoryType"), physicalMemory.getMemoryType());
            addGridRow(physicalMemoryGrid, row++, I18N.get("memory.clockSpeed"),
                physicalMemory.getClockSpeed() > 0 ? String.format("%.0f MHz", physicalMemory.getClockSpeed() / 1_000_000.0) : na);
            content.getChildren().add(physicalMemoryGrid);
        }
        content.getChildren().add(new Separator());

        // Storage section
        content.getChildren().add(createSectionLabel(MaterialDesignH.HARDDISK, I18N.get("tab.storage")));
        index = 0;
        for (HWDiskStore diskStore : diskStores) {
            content.getChildren().add(createSectionLabel(I18N.get("detail.diskStore") + " #" + index++));
            GridPane diskGrid = createInfoGrid();
            row = 0;
            addGridRow(diskGrid, row++, I18N.get("storage.name"), diskStore.getName());
            addGridRow(diskGrid, row++, I18N.get("storage.model"), diskStore.getModel());
            addGridRow(diskGrid, row++, I18N.get("storage.serial"), diskStore.getSerial());
            addGridRow(diskGrid, row++, I18N.get("storage.size"), SystemInfoService.formatBytes(diskStore.getSize()));
            int partitionIndex = 0;
            for (HWPartition partition : diskStore.getPartitions()) {
                String partitionText = String.format("%s, %s, %s:%s, %s @ %s",
                    partition.getIdentification(),
                    partition.getType(),
                    partition.getMajor(),
                    partition.getMinor(),
                    SystemInfoService.formatBytes(partition.getSize()),
                    partition.getMountPoint());
                addGridRow(diskGrid, row++, I18N.get("detail.partition") + " #" + partitionIndex++, partitionText);
            }
            if (partitionIndex == 0) {
                addGridRow(diskGrid, row++, I18N.get("detail.partition"), na);
            }
            content.getChildren().add(diskGrid);
        }
        content.getChildren().add(new Separator());

        // Graphics section
        content.getChildren().add(createSectionLabel(MaterialDesignH.HDMI_PORT, I18N.get("overview.graphicsCard")));
        index = 0;
        for (GraphicsCard graphicsCard : graphicsCards) {
            content.getChildren().add(createSectionLabel(I18N.get("overview.graphicsCard") + " #" + index++));
            GridPane graphicsGrid = createInfoGrid();
            row = 0;
            addGridRow(graphicsGrid, row++, I18N.get("cpu.name"), graphicsCard.getName());
            addGridRow(graphicsGrid, row++, I18N.get("detail.deviceId"), graphicsCard.getDeviceId());
            addGridRow(graphicsGrid, row++, I18N.get("cpu.vendor"), graphicsCard.getVendor());
            addGridRow(graphicsGrid, row++, I18N.get("detail.version"), graphicsCard.getVersionInfo());
            addGridRow(graphicsGrid, row++, I18N.get("detail.vram"), SystemInfoService.formatBytes(graphicsCard.getVRam()));
            content.getChildren().add(graphicsGrid);
        }
        content.getChildren().add(new Separator());

        // Display section
        content.getChildren().add(createSectionLabel(MaterialDesignM.MONITOR, I18N.get("overview.display")));
        index = 0;
        for (Display display : displays) {
            byte[] edid = display.getEdid();
            content.getChildren().add(createSectionLabel(I18N.get("overview.display") + " #" + index++));
            GridPane displayGrid = createInfoGrid();
            row = 0;
            if (edid != null && edid.length >= 128) {
                addGridRow(displayGrid, row++, I18N.get("detail.manufacturer"), EdidUtil.getManufacturerID(edid));
                addGridRow(displayGrid, row++, I18N.get("detail.model"), EdidUtil.getModel(edid));
                addGridRow(displayGrid, row++, I18N.get("detail.serialNumber"), EdidUtil.getSerialNo(edid));
                addGridRow(displayGrid, row++, I18N.get("detail.resolution"), EdidUtil.getPreferredResolution(edid));
            } else {
                addGridRow(displayGrid, row++, I18N.get("overview.display"), I18N.get("overview.unknownDisplay"));
            }
            addGridRow(displayGrid, row++, I18N.get("detail.rawEdid"), edid != null ? toHex(edid) : na);
            content.getChildren().add(displayGrid);
        }
        if (displays.isEmpty()) {
            GridPane displayGrid = createInfoGrid();
            addGridRow(displayGrid, 0, I18N.get("detail.rawEdid"), na);
            content.getChildren().add(displayGrid);
        }
        content.getChildren().add(new Separator());

        // Sound section
        content.getChildren().add(createSectionLabel(MaterialDesignV.VOLUME_HIGH, I18N.get("overview.soundCard")));
        index = 0;
        for (SoundCard soundCard : soundCards) {
            content.getChildren().add(createSectionLabel(I18N.get("overview.soundCard") + " #" + index++));
            GridPane soundGrid = createInfoGrid();
            row = 0;
            addGridRow(soundGrid, row++, I18N.get("cpu.name"), soundCard.getName());
            addGridRow(soundGrid, row++, I18N.get("detail.driverVersion"), soundCard.getDriverVersion());
            addGridRow(soundGrid, row++, I18N.get("detail.codec"), soundCard.getCodec());
            content.getChildren().add(soundGrid);
        }
        content.getChildren().add(new Separator());

        // Network section
        content.getChildren().add(createSectionLabel(MaterialDesignW.WEB, I18N.get("tab.network")));
        NetworkParams networkParams = operatingSystem.getNetworkParams();
        GridPane networkGrid = createInfoGrid();
        row = 0;
        addGridRow(networkGrid, row++, I18N.get("detail.domainName"), networkParams.getDomainName());
        addGridRow(networkGrid, row++, I18N.get("detail.hostName"), networkParams.getHostName());
        addGridRow(networkGrid, row++, I18N.get("detail.ipv4DefaultGateway"), networkParams.getIpv4DefaultGateway());
        addGridRow(networkGrid, row++, I18N.get("detail.ipv6DefaultGateway"), networkParams.getIpv6DefaultGateway());
        addGridRow(networkGrid, row++, I18N.get("detail.dnsServers"), Arrays.toString(networkParams.getDnsServers()));
        content.getChildren().add(networkGrid);
        index = 0;
        for (NetworkIF networkIF : networkIFs) {
            networkIF.updateAttributes();
            content.getChildren().add(createSectionLabel(I18N.get("detail.networkInterface") + " #" + index++));
            GridPane interfaceGrid = createInfoGrid();
            row = 0;
            addGridRow(interfaceGrid, row++, I18N.get("network.name"), networkIF.getName() + " (" + networkIF.getDisplayName() + ")");
            addGridRow(interfaceGrid, row++, I18N.get("detail.ifAlias"), networkIF.getIfAlias());
            addGridRow(interfaceGrid, row++, I18N.get("network.mac"), networkIF.getMacaddr());
            addGridRow(interfaceGrid, row++, I18N.get("detail.mtu"), String.valueOf(networkIF.getMTU()));
            addGridRow(interfaceGrid, row++, I18N.get("network.speed"),
                networkIF.getSpeed() > 0 ? SystemInfoService.formatBytes(networkIF.getSpeed() / 8) + "/s" : na);
            addGridRow(interfaceGrid, row++, I18N.get("network.ipv4"), Arrays.toString(networkIF.getIPv4addr()));
            addGridRow(interfaceGrid, row++, I18N.get("network.ipv6"), Arrays.toString(networkIF.getIPv6addr()));
            addGridRow(interfaceGrid, row++, I18N.get("detail.traffic"),
                String.format("%s %d/%s (%d %s, %d %s); %s %d/%s (%d %s, %d %s)",
                    I18N.get("detail.received"),
                    networkIF.getPacketsRecv(),
                    SystemInfoService.formatBytes(networkIF.getBytesRecv()),
                    networkIF.getInErrors(),
                    I18N.get("detail.err"),
                    networkIF.getInDrops(),
                    I18N.get("detail.drop"),
                    I18N.get("detail.transmitted"),
                    networkIF.getPacketsSent(),
                    SystemInfoService.formatBytes(networkIF.getBytesSent()),
                    networkIF.getOutErrors(),
                    I18N.get("detail.err"),
                    networkIF.getCollisions(),
                    I18N.get("detail.coll")));
            content.getChildren().add(interfaceGrid);
        }
        content.getChildren().add(new Separator());

        // Power section
        content.getChildren().add(createSectionLabel(MaterialDesignB.BATTERY, I18N.get("overview.powerSource")));
        index = 0;
        for (PowerSource powerSource : powerSources) {
            powerSource.updateAttributes();
            content.getChildren().add(createSectionLabel(I18N.get("overview.powerSource") + " #" + index++));
            GridPane powerGrid = createInfoGrid();
            row = 0;
            addGridRow(powerGrid, row++, I18N.get("power.name"), powerSource.getName());
            addGridRow(powerGrid, row++, I18N.get("power.deviceName"), powerSource.getDeviceName());
            addGridRow(powerGrid, row++, I18N.get("power.remainingCapacityPercent"),
                powerSource.getRemainingCapacityPercent() >= 0
                    ? String.format("%.1f%%", powerSource.getRemainingCapacityPercent() * 100)
                    : na);
            double timeRemaining = powerSource.getTimeRemainingEstimated();
            String timeStr;
            if (timeRemaining < -1) {
                timeStr = I18N.get("power.unlimited");
            } else if (timeRemaining < 0) {
                timeStr = powerSource.isPowerOnLine() ? I18N.get("power.unlimited") : I18N.get("power.calculating");
            } else {
                timeStr = SystemInfoService.formatUptime((long) timeRemaining);
            }
            addGridRow(powerGrid, row++, I18N.get("power.timeRemainingEstimated"), timeStr);
            addGridRow(powerGrid, row++, I18N.get("power.timeRemainingInstant"),
                powerSource.getTimeRemainingInstant() >= 0
                    ? SystemInfoService.formatUptime((long) powerSource.getTimeRemainingInstant())
                    : na);
            addGridRow(powerGrid, row++, I18N.get("power.voltage"),
                powerSource.getVoltage() >= 0 ? String.format("%.1f V", powerSource.getVoltage()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.amperage"),
                powerSource.getAmperage() >= 0 ? String.format("%.1f mA", powerSource.getAmperage()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.powerUsageRate"),
                powerSource.getPowerUsageRate() >= 0 ? String.format("%.1f mW", powerSource.getPowerUsageRate()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.powerOnLine"), String.valueOf(powerSource.isPowerOnLine()));
            addGridRow(powerGrid, row++, I18N.get("power.charging"), String.valueOf(powerSource.isCharging()));
            addGridRow(powerGrid, row++, I18N.get("power.discharging"), String.valueOf(powerSource.isDischarging()));
            addGridRow(powerGrid, row++, I18N.get("power.capacityUnits"), String.valueOf(powerSource.getCapacityUnits()));
            addGridRow(powerGrid, row++, I18N.get("power.currentCapacity"),
                powerSource.getCurrentCapacity() > 0 ? String.valueOf(powerSource.getCurrentCapacity()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.maxCapacity"),
                powerSource.getMaxCapacity() > 0 ? String.valueOf(powerSource.getMaxCapacity()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.designCapacity"),
                powerSource.getDesignCapacity() > 0 ? String.valueOf(powerSource.getDesignCapacity()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.cycleCount"),
                powerSource.getCycleCount() >= 0 ? String.valueOf(powerSource.getCycleCount()) : na);
            addGridRow(powerGrid, row++, I18N.get("power.chemistry"), powerSource.getChemistry());
            addGridRow(powerGrid, row++, I18N.get("power.manufacturer"), powerSource.getManufacturer());
            addGridRow(powerGrid, row++, I18N.get("power.serialNumber"), powerSource.getSerialNumber());
            addGridRow(powerGrid, row++, I18N.get("power.temperature"),
                powerSource.getTemperature() > 0 ? String.format("%.1f °C", powerSource.getTemperature()) : na);
            content.getChildren().add(powerGrid);
        }
        if (powerSources.isEmpty()) {
            GridPane powerGrid = createInfoGrid();
            addGridRow(powerGrid, 0, I18N.get("power.info"), na);
            content.getChildren().add(powerGrid);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    @Override
    protected void addGridRow(GridPane grid, int row, String key, String value) {
        addReadOnlyGridRow(grid, row, key, value);
    }

    private String toHex(byte[] data) {
        if (data == null || data.length == 0) {
            return I18N.get("power.notAvailable");
        }
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
