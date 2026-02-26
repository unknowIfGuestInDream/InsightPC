package com.tlcsdm.insightpc.controller;

import com.tlcsdm.insightpc.config.AppSettings;
import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Main controller for the InsightPC application.
 * Manages the tabbed OSHI information panels.
 */
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @FXML
    private TabPane tabPane;

    private Stage primaryStage;
    private SystemInfoService systemInfoService;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        systemInfoService = new SystemInfoService();
        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "insightpc-refresh");
            t.setDaemon(true);
            return t;
        });

        buildOverviewTab();
        buildDetailTab();
        buildMemoryTab();
        buildCpuTab();
        buildStorageTab();
        buildNetworkTab();
        buildVariablesTab();
        buildProcessTab();
        buildUsbDevicesTab();
        buildPowerTab();

        LOG.info("All tabs initialized");
    }

    /**
     * Set the primary stage reference.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Open the settings dialog.
     */
    @FXML
    public void openSettings() {
        AppSettings.getInstance().getPreferencesFx().show(true);
    }

    /**
     * Exit the application.
     */
    @FXML
    public void exitApplication() {
        shutdown();
        if (primaryStage != null) {
            primaryStage.close();
        }
    }

    /**
     * Show about dialog.
     */
    @FXML
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18N.get("menu.about"));
        alert.setHeaderText(I18N.get("app.title"));
        alert.setContentText(I18N.get("about.description"));
        FontIcon icon = new FontIcon(Material.COMPUTER);
        icon.setIconSize(48);
        alert.setGraphic(icon);
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }
        alert.showAndWait();
    }

    /**
     * Shutdown and cleanup resources.
     */
    public void shutdown() {
        LOG.info("Application shutting down");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    // --- Tab builders ---

    private void buildOverviewTab() {
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
        tabPane.getTabs().add(tab);
    }

    private void buildDetailTab() {
        Tab tab = new Tab(I18N.get("tab.detail"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.INFO));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ComputerSystem cs = systemInfoService.getComputerSystem();

        // Computer System section
        content.getChildren().add(createSectionLabel(I18N.get("detail.computerSystem")));
        GridPane csGrid = createInfoGrid();
        int row = 0;
        addGridRow(csGrid, row++, I18N.get("detail.manufacturer"), cs.getManufacturer());
        addGridRow(csGrid, row++, I18N.get("detail.model"), cs.getModel());
        addGridRow(csGrid, row++, I18N.get("detail.serialNumber"), cs.getSerialNumber());
        addGridRow(csGrid, row++, I18N.get("detail.hardwareUUID"), cs.getHardwareUUID());
        content.getChildren().add(csGrid);

        // Baseboard section
        Baseboard baseboard = cs.getBaseboard();
        content.getChildren().add(createSectionLabel(I18N.get("detail.baseboard")));
        GridPane bbGrid = createInfoGrid();
        row = 0;
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardManufacturer"), baseboard.getManufacturer());
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardModel"), baseboard.getModel());
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardVersion"), baseboard.getVersion());
        addGridRow(bbGrid, row++, I18N.get("detail.baseboardSerialNumber"), baseboard.getSerialNumber());
        content.getChildren().add(bbGrid);

        // Firmware section
        Firmware firmware = cs.getFirmware();
        content.getChildren().add(createSectionLabel(I18N.get("detail.firmware")));
        GridPane fwGrid = createInfoGrid();
        row = 0;
        addGridRow(fwGrid, row++, I18N.get("detail.firmwareManufacturer"), firmware.getManufacturer());
        addGridRow(fwGrid, row++, I18N.get("detail.firmwareName"), firmware.getName());
        addGridRow(fwGrid, row++, I18N.get("detail.firmwareVersion"), firmware.getVersion());
        addGridRow(fwGrid, row++, I18N.get("detail.firmwareReleaseDate"), firmware.getReleaseDate());
        addGridRow(fwGrid, row++, I18N.get("detail.firmwareDescription"), firmware.getDescription());
        content.getChildren().add(fwGrid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildMemoryTab() {
        Tab tab = new Tab(I18N.get("tab.memory"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.MEMORY));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        GlobalMemory memory = systemInfoService.getMemory();

        // Physical memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.physical")));
        GridPane grid = createInfoGrid();
        int row = 0;
        addGridRow(grid, row++, I18N.get("memory.total"),
            SystemInfoService.formatBytes(memory.getTotal()));
        addGridRow(grid, row++, I18N.get("memory.available"),
            SystemInfoService.formatBytes(memory.getAvailable()));
        addGridRow(grid, row++, I18N.get("memory.used"),
            SystemInfoService.formatBytes(memory.getTotal() - memory.getAvailable()));
        addGridRow(grid, row++, I18N.get("memory.pageSize"),
            SystemInfoService.formatBytes(memory.getPageSize()));
        content.getChildren().add(grid);

        // Memory usage bar
        ProgressBar memBar = new ProgressBar(0);
        memBar.setMaxWidth(Double.MAX_VALUE);
        memBar.setPrefHeight(25);
        Label memUsageLabel = new Label();
        HBox usageBox = new HBox(10, memBar, memUsageLabel);
        usageBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(memBar, Priority.ALWAYS);
        content.getChildren().add(usageBox);

        // Virtual memory section
        content.getChildren().add(createSectionLabel(I18N.get("memory.virtual")));
        VirtualMemory vm = memory.getVirtualMemory();
        GridPane vmGrid = createInfoGrid();
        row = 0;
        addGridRow(vmGrid, row++, I18N.get("memory.swapTotal"),
            SystemInfoService.formatBytes(vm.getSwapTotal()));
        addGridRow(vmGrid, row++, I18N.get("memory.swapUsed"),
            SystemInfoService.formatBytes(vm.getSwapUsed()));
        addGridRow(vmGrid, row++, I18N.get("memory.virtualMax"),
            SystemInfoService.formatBytes(vm.getVirtualMax()));
        addGridRow(vmGrid, row++, I18N.get("memory.virtualInUse"),
            SystemInfoService.formatBytes(vm.getVirtualInUse()));
        content.getChildren().add(vmGrid);

        // Physical memory sticks
        List<PhysicalMemory> physMems = memory.getPhysicalMemory();
        if (!physMems.isEmpty()) {
            content.getChildren().add(createSectionLabel(I18N.get("memory.sticks")));
            for (PhysicalMemory pm : physMems) {
                GridPane pmGrid = createInfoGrid();
                row = 0;
                addGridRow(pmGrid, row++, I18N.get("memory.bankLabel"), pm.getBankLabel());
                addGridRow(pmGrid, row++, I18N.get("memory.capacity"),
                    SystemInfoService.formatBytes(pm.getCapacity()));
                addGridRow(pmGrid, row++, I18N.get("memory.clockSpeed"),
                    String.format("%.0f MHz", pm.getClockSpeed() / 1_000_000.0));
                addGridRow(pmGrid, row++, I18N.get("memory.memoryType"), pm.getMemoryType());
                content.getChildren().add(pmGrid);
            }
        }

        // Schedule memory usage updates
        scheduler.scheduleAtFixedRate(() -> {
            GlobalMemory mem = systemInfoService.getMemory();
            long total = mem.getTotal();
            long available = mem.getAvailable();
            double usage = total > 0 ? (double) (total - available) / total : 0;
            Platform.runLater(() -> {
                memBar.setProgress(usage);
                memUsageLabel.setText(String.format("%.1f%% (%s / %s)",
                    usage * 100,
                    SystemInfoService.formatBytes(total - available),
                    SystemInfoService.formatBytes(total)));
            });
        }, 0, 3, TimeUnit.SECONDS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildCpuTab() {
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
        tabPane.getTabs().add(tab);
    }

    private void buildStorageTab() {
        Tab tab = new Tab(I18N.get("tab.storage"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.SD_STORAGE));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<HWDiskStore> diskStores = systemInfoService.getDiskStores();

        content.getChildren().add(createSectionLabel(I18N.get("storage.info")));

        for (HWDiskStore disk : diskStores) {
            GridPane grid = createInfoGrid();
            int row = 0;
            addGridRow(grid, row++, I18N.get("storage.name"), disk.getName());
            addGridRow(grid, row++, I18N.get("storage.model"), disk.getModel());
            addGridRow(grid, row++, I18N.get("storage.serial"), disk.getSerial());
            addGridRow(grid, row++, I18N.get("storage.size"),
                SystemInfoService.formatBytes(disk.getSize()));
            addGridRow(grid, row++, I18N.get("storage.reads"),
                String.valueOf(disk.getReads()));
            addGridRow(grid, row++, I18N.get("storage.writes"),
                String.valueOf(disk.getWrites()));
            content.getChildren().add(grid);
            content.getChildren().add(new Separator());
        }

        // File system info
        content.getChildren().add(createSectionLabel(I18N.get("storage.fileSystem")));
        systemInfoService.getOperatingSystem().getFileSystem().getFileStores().forEach(fs -> {
            GridPane fsGrid = createInfoGrid();
            int row = 0;
            addGridRow(fsGrid, row++, I18N.get("storage.mount"), fs.getMount());
            addGridRow(fsGrid, row++, I18N.get("storage.fsType"), fs.getType());
            addGridRow(fsGrid, row++, I18N.get("storage.totalSpace"),
                SystemInfoService.formatBytes(fs.getTotalSpace()));
            addGridRow(fsGrid, row++, I18N.get("storage.usableSpace"),
                SystemInfoService.formatBytes(fs.getUsableSpace()));

            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            if (total > 0) {
                ProgressBar bar = new ProgressBar((double) (total - usable) / total);
                bar.setMaxWidth(Double.MAX_VALUE);
                bar.setPrefHeight(20);
                content.getChildren().add(fsGrid);
                content.getChildren().add(bar);
            } else {
                content.getChildren().add(fsGrid);
            }
            content.getChildren().add(new Separator());
        });

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildNetworkTab() {
        Tab tab = new Tab(I18N.get("tab.network"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.NETWORK_WIFI));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<NetworkIF> networkIFs = systemInfoService.getNetworkInterfaces();

        content.getChildren().add(createSectionLabel(I18N.get("network.info")));

        for (NetworkIF net : networkIFs) {
            GridPane grid = createInfoGrid();
            int row = 0;
            addGridRow(grid, row++, I18N.get("network.name"), net.getName());
            addGridRow(grid, row++, I18N.get("network.displayName"), net.getDisplayName());
            addGridRow(grid, row++, I18N.get("network.mac"), net.getMacaddr());
            addGridRow(grid, row++, I18N.get("network.speed"),
                net.getSpeed() > 0
                    ? SystemInfoService.formatBytes(net.getSpeed() / 8) + "/s"
                    : "N/A");
            addGridRow(grid, row++, I18N.get("network.ipv4"),
                String.join(", ", net.getIPv4addr()));
            addGridRow(grid, row++, I18N.get("network.ipv6"),
                String.join(", ", net.getIPv6addr()));
            addGridRow(grid, row++, I18N.get("network.bytesRecv"),
                SystemInfoService.formatBytes(net.getBytesRecv()));
            addGridRow(grid, row++, I18N.get("network.bytesSent"),
                SystemInfoService.formatBytes(net.getBytesSent()));
            content.getChildren().add(grid);
            content.getChildren().add(new Separator());
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildVariablesTab() {
        Tab tab = new Tab(I18N.get("tab.variables"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.CODE));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        content.getChildren().add(createSectionLabel(I18N.get("variables.info")));

        TableView<Map.Entry<String, String>> envTable = new TableView<>();
        envTable.setPrefHeight(500);

        TableColumn<Map.Entry<String, String>, String> nameCol = new TableColumn<>(I18N.get("variables.name"));
        nameCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(p.getValue().getKey()));
        nameCol.setPrefWidth(250);

        TableColumn<Map.Entry<String, String>, String> valueCol = new TableColumn<>(I18N.get("variables.value"));
        valueCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(p.getValue().getValue()));
        valueCol.setPrefWidth(600);

        envTable.getColumns().addAll(nameCol, valueCol);

        Map<String, String> sortedEnv = new TreeMap<>(System.getenv());
        envTable.getItems().addAll(sortedEnv.entrySet());

        content.getChildren().add(envTable);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildProcessTab() {
        Tab tab = new Tab(I18N.get("tab.processes"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.APPS));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        OperatingSystem os = systemInfoService.getOperatingSystem();

        content.getChildren().add(createSectionLabel(I18N.get("process.summary")));
        GridPane summaryGrid = createInfoGrid();
        addGridRow(summaryGrid, 0, I18N.get("process.count"),
            String.valueOf(os.getProcessCount()));
        addGridRow(summaryGrid, 1, I18N.get("process.threadCount"),
            String.valueOf(os.getThreadCount()));
        content.getChildren().add(summaryGrid);

        // Process table
        content.getChildren().add(createSectionLabel(I18N.get("process.list")));

        TableView<OSProcess> processTable = new TableView<>();
        processTable.setPrefHeight(400);

        TableColumn<OSProcess, Number> pidCol = new TableColumn<>(I18N.get("process.pid"));
        pidCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleLongProperty(p.getValue().getProcessID()));
        pidCol.setPrefWidth(70);

        TableColumn<OSProcess, String> nameCol = new TableColumn<>(I18N.get("process.name"));
        nameCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(p.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<OSProcess, String> stateCol = new TableColumn<>(I18N.get("process.state"));
        stateCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(p.getValue().getState().name()));
        stateCol.setPrefWidth(100);

        TableColumn<OSProcess, String> memCol = new TableColumn<>(I18N.get("process.memory"));
        memCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(
                SystemInfoService.formatBytes(p.getValue().getResidentSetSize())));
        memCol.setPrefWidth(100);

        TableColumn<OSProcess, String> cpuCol = new TableColumn<>(I18N.get("process.cpuPercent"));
        cpuCol.setCellValueFactory(p ->
            new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f%%",
                    100d * p.getValue().getProcessCpuLoadCumulative())));
        cpuCol.setPrefWidth(80);

        processTable.getColumns().addAll(pidCol, nameCol, stateCol, memCol, cpuCol);

        // Load initial process list (top 50 by memory)
        List<OSProcess> processes = os.getProcesses(
            OperatingSystem.ProcessFiltering.ALL_PROCESSES,
            OperatingSystem.ProcessSorting.RSS_DESC,
            50);
        processTable.getItems().addAll(processes);

        Button refreshBtn = new Button(I18N.get("process.refresh"));
        refreshBtn.setOnAction(e -> {
            processTable.getItems().clear();
            List<OSProcess> refreshedProcesses = systemInfoService.getOperatingSystem().getProcesses(
                OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.RSS_DESC,
                50);
            processTable.getItems().addAll(refreshedProcesses);
        });

        content.getChildren().addAll(processTable, refreshBtn);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildUsbDevicesTab() {
        Tab tab = new Tab(I18N.get("tab.usbDevices"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.USB));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        content.getChildren().add(createSectionLabel(I18N.get("usb.info")));

        List<UsbDevice> usbDevices = systemInfoService.getUsbDevices();
        TreeView<String> usbTree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>(I18N.get("usb.info"));
        rootItem.setExpanded(true);

        for (UsbDevice device : usbDevices) {
            buildUsbTreeItem(rootItem, device);
        }

        usbTree.setRoot(rootItem);
        usbTree.setPrefHeight(500);
        content.getChildren().add(usbTree);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildUsbTreeItem(TreeItem<String> parent, UsbDevice device) {
        String label = device.getName();
        if (device.getVendor() != null && !device.getVendor().isEmpty()) {
            label += " (" + device.getVendor() + ")";
        }
        TreeItem<String> item = new TreeItem<>(label);
        item.setExpanded(true);

        for (UsbDevice child : device.getConnectedDevices()) {
            buildUsbTreeItem(item, child);
        }

        parent.getChildren().add(item);
    }

    private void buildPowerTab() {
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
        tabPane.getTabs().add(tab);
    }

    // --- Helper methods ---

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        return label;
    }

    private GridPane createInfoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 0, 5, 10));
        ColumnConstraints keyCol = new ColumnConstraints();
        keyCol.setMinWidth(160);
        keyCol.setPrefWidth(180);
        ColumnConstraints valCol = new ColumnConstraints();
        valCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(keyCol, valCol);
        return grid;
    }

    private void addGridRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.setStyle("-fx-font-weight: bold;");
        Label valLabel = new Label(value != null ? value : "N/A");
        valLabel.setWrapText(true);
        grid.add(keyLabel, 0, row);
        grid.add(valLabel, 1, row);
    }

    private FontIcon createTabIcon(Material icon) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        return fontIcon;
    }

    private HBox createOverviewRow(Material icon, String label, String value) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameLabel.setMinWidth(120);
        nameLabel.setPrefWidth(120);

        Label valueLabel = new Label(value != null ? value : "N/A");
        valueLabel.setWrapText(true);

        HBox row = new HBox(10, fontIcon, nameLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 4;");
        return row;
    }
}
