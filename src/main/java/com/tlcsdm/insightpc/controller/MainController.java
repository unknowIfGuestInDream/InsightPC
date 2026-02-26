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
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main controller for the InsightPC application.
 * Manages the tabbed OSHI information panels.
 */
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @FXML
    private Label statusLabel;

    @FXML
    private TabPane tabPane;

    private Stage primaryStage;
    private SystemInfoService systemInfoService;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        statusLabel.setText(I18N.get("status.ready"));
        systemInfoService = new SystemInfoService();
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "insightpc-refresh");
            t.setDaemon(true);
            return t;
        });

        buildOverviewTab();
        buildCpuTab();
        buildMemoryTab();
        buildDiskTab();
        buildNetworkTab();
        buildProcessTab();

        statusLabel.setText(I18N.get("status.loaded"));
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

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        OperatingSystem os = systemInfoService.getOperatingSystem();
        ComputerSystem cs = systemInfoService.getComputerSystem();
        CentralProcessor cpu = systemInfoService.getProcessor();
        GlobalMemory memory = systemInfoService.getMemory();

        // System overview section
        content.getChildren().add(createSectionLabel(I18N.get("overview.system")));
        GridPane sysGrid = createInfoGrid();
        int row = 0;
        addGridRow(sysGrid, row++, I18N.get("overview.os"), os.toString());
        addGridRow(sysGrid, row++, I18N.get("overview.manufacturer"), cs.getManufacturer());
        addGridRow(sysGrid, row++, I18N.get("overview.model"), cs.getModel());
        addGridRow(sysGrid, row++, I18N.get("overview.uptime"),
            SystemInfoService.formatUptime(systemInfoService.getSystemUptime()));
        content.getChildren().add(sysGrid);

        // CPU overview section
        content.getChildren().add(createSectionLabel(I18N.get("overview.cpu")));
        GridPane cpuGrid = createInfoGrid();
        row = 0;
        addGridRow(cpuGrid, row++, I18N.get("cpu.name"),
            cpu.getProcessorIdentifier().getName());
        addGridRow(cpuGrid, row++, I18N.get("cpu.physicalCores"),
            String.valueOf(cpu.getPhysicalProcessorCount()));
        addGridRow(cpuGrid, row++, I18N.get("cpu.logicalCores"),
            String.valueOf(cpu.getLogicalProcessorCount()));
        content.getChildren().add(cpuGrid);

        // Memory overview section
        content.getChildren().add(createSectionLabel(I18N.get("overview.memory")));
        GridPane memGrid = createInfoGrid();
        row = 0;
        addGridRow(memGrid, row++, I18N.get("memory.total"),
            SystemInfoService.formatBytes(memory.getTotal()));
        addGridRow(memGrid, row++, I18N.get("memory.available"),
            SystemInfoService.formatBytes(memory.getAvailable()));
        addGridRow(memGrid, row++, I18N.get("memory.used"),
            SystemInfoService.formatBytes(memory.getTotal() - memory.getAvailable()));
        content.getChildren().add(memGrid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void buildCpuTab() {
        Tab tab = new Tab(I18N.get("tab.cpu"));
        tab.setClosable(false);

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

    private void buildMemoryTab() {
        Tab tab = new Tab(I18N.get("tab.memory"));
        tab.setClosable(false);

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

    private void buildDiskTab() {
        Tab tab = new Tab(I18N.get("tab.disk"));
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<HWDiskStore> diskStores = systemInfoService.getDiskStores();

        content.getChildren().add(createSectionLabel(I18N.get("disk.info")));

        for (HWDiskStore disk : diskStores) {
            GridPane grid = createInfoGrid();
            int row = 0;
            addGridRow(grid, row++, I18N.get("disk.name"), disk.getName());
            addGridRow(grid, row++, I18N.get("disk.model"), disk.getModel());
            addGridRow(grid, row++, I18N.get("disk.serial"), disk.getSerial());
            addGridRow(grid, row++, I18N.get("disk.size"),
                SystemInfoService.formatBytes(disk.getSize()));
            addGridRow(grid, row++, I18N.get("disk.reads"),
                String.valueOf(disk.getReads()));
            addGridRow(grid, row++, I18N.get("disk.writes"),
                String.valueOf(disk.getWrites()));
            content.getChildren().add(grid);
            content.getChildren().add(new Separator());
        }

        // File system info
        content.getChildren().add(createSectionLabel(I18N.get("disk.fileSystem")));
        systemInfoService.getOperatingSystem().getFileSystem().getFileStores().forEach(fs -> {
            GridPane fsGrid = createInfoGrid();
            int row = 0;
            addGridRow(fsGrid, row++, I18N.get("disk.mount"), fs.getMount());
            addGridRow(fsGrid, row++, I18N.get("disk.fsType"), fs.getType());
            addGridRow(fsGrid, row++, I18N.get("disk.totalSpace"),
                SystemInfoService.formatBytes(fs.getTotalSpace()));
            addGridRow(fsGrid, row++, I18N.get("disk.usableSpace"),
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

    private void buildProcessTab() {
        Tab tab = new Tab(I18N.get("tab.processes"));
        tab.setClosable(false);

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
}
