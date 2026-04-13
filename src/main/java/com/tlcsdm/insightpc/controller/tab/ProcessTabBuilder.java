package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.embed.swing.SwingFXUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * Builds the Processes tab showing running processes.
 */
public class ProcessTabBuilder extends AbstractTabBuilder {

    private static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 2;
    private static final double NANOS_PER_SECOND = 1_000_000_000d;
    private static final int PROCESS_ICON_SIZE = 16;

    public ProcessTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.processes"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignA.APPS));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        OperatingSystem os = systemInfoService.getOperatingSystem();

        content.getChildren().add(createSectionLabel(I18N.get("process.summary")));
        GridPane summaryGrid = createInfoGrid();
        content.getChildren().add(summaryGrid);

        content.getChildren().add(createSectionLabel(I18N.get("process.list")));

        TableView<ProcessRow> processTable = new TableView<>();
        processTable.setPrefHeight(420);

        TableColumn<ProcessRow, Image> iconCol = new TableColumn<>(I18N.get("process.icon"));
        iconCol.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().icon()));
        iconCol.setPrefWidth(34);
        iconCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label fallback = new Label();

            {
                imageView.setFitWidth(PROCESS_ICON_SIZE);
                imageView.setFitHeight(PROCESS_ICON_SIZE);
                imageView.setPreserveRatio(true);
                fallback.setGraphic(new FontIcon(MaterialDesignA.APPS));
                fallback.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else if (item != null) {
                    imageView.setImage(item);
                    setGraphic(imageView);
                } else {
                    setGraphic(fallback);
                }
            }
        });

        TableColumn<ProcessRow, Number> pidCol = new TableColumn<>(I18N.get("process.pid"));
        pidCol.setCellValueFactory(p -> new SimpleLongProperty(p.getValue().pid()));
        pidCol.setPrefWidth(70);

        TableColumn<ProcessRow, Number> ppidCol = new TableColumn<>(I18N.get("process.ppid"));
        ppidCol.setCellValueFactory(p -> new SimpleLongProperty(p.getValue().ppid()));
        ppidCol.setPrefWidth(70);

        TableColumn<ProcessRow, Number> threadsCol = new TableColumn<>(I18N.get("process.threads"));
        threadsCol.setCellValueFactory(p -> new SimpleLongProperty(p.getValue().threads()));
        threadsCol.setPrefWidth(80);

        TableColumn<ProcessRow, String> cpuCol = new TableColumn<>(I18N.get("process.cpuPercent"));
        cpuCol.setCellValueFactory(p -> new SimpleStringProperty(formatPercentValue(p.getValue().cpuPercent())));
        cpuCol.setPrefWidth(90);

        TableColumn<ProcessRow, String> cumulativeCpuCol = new TableColumn<>(I18N.get("process.cumulativeCpu"));
        cumulativeCpuCol.setCellValueFactory(p ->
            new SimpleStringProperty(formatPercentValue(p.getValue().cumulativeCpuPercent())));
        cumulativeCpuCol.setPrefWidth(120);

        TableColumn<ProcessRow, String> memPercentCol = new TableColumn<>(I18N.get("process.memoryPercent"));
        memPercentCol.setCellValueFactory(p -> new SimpleStringProperty(formatPercentValue(p.getValue().memoryPercent())));
        memPercentCol.setPrefWidth(100);

        TableColumn<ProcessRow, String> virtualMemoryCol = new TableColumn<>(I18N.get("process.virtualMemory"));
        virtualMemoryCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().virtualMemory()));
        virtualMemoryCol.setPrefWidth(120);

        TableColumn<ProcessRow, String> memCol = new TableColumn<>(I18N.get("process.memory"));
        memCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().residentMemory()));
        memCol.setPrefWidth(120);

        TableColumn<ProcessRow, String> nameCol = new TableColumn<>(I18N.get("process.name"));
        nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().name()));
        nameCol.setPrefWidth(260);

        processTable.getColumns().addAll(
            iconCol,
            pidCol,
            ppidCol,
            threadsCol,
            cpuCol,
            cumulativeCpuCol,
            memPercentCol,
            virtualMemoryCol,
            memCol,
            nameCol);

        Label sortLabel = new Label(I18N.get("process.sortBy"));
        ToggleGroup sortGroup = new ToggleGroup();
        RadioButton cpuSortBtn = createRadioButton(I18N.get("process.sort.cpu"), ProcessSort.CPU, sortGroup);
        RadioButton cumulativeCpuSortBtn =
            createRadioButton(I18N.get("process.sort.cumulativeCpu"), ProcessSort.CUMULATIVE_CPU, sortGroup);
        RadioButton memorySortBtn = createRadioButton(I18N.get("process.sort.memory"), ProcessSort.MEMORY, sortGroup);
        cpuSortBtn.setSelected(true);

        HBox sortBox = new HBox(10, sortLabel, cpuSortBtn, cumulativeCpuSortBtn, memorySortBtn);

        Label cpuScopeLabel = new Label(I18N.get("process.cpuScope"));
        ToggleGroup cpuScopeGroup = new ToggleGroup();
        RadioButton oneProcessorBtn = createRadioButton(
            I18N.get("process.cpuScope.oneProcessor"), CpuPercentScope.ONE_PROCESSOR, cpuScopeGroup);
        RadioButton systemBtn = createRadioButton(I18N.get("process.cpuScope.system"), CpuPercentScope.SYSTEM, cpuScopeGroup);
        systemBtn.setSelected(true);
        HBox cpuScopeBox = new HBox(8, cpuScopeLabel, oneProcessorBtn, systemBtn);

        Label refreshIntervalLabel = new Label(I18N.get("process.refreshInterval"));
        ComboBox<Integer> refreshIntervalBox = new ComboBox<>();
        refreshIntervalBox.getItems().addAll(1, 2, 3, 5, 10);
        refreshIntervalBox.setValue(DEFAULT_REFRESH_INTERVAL_SECONDS);
        refreshIntervalBox.setPrefWidth(80);

        CheckBox autoRefreshCheckBox = new CheckBox(I18N.get("process.autoRefresh"));
        autoRefreshCheckBox.setSelected(true);

        Button refreshBtn = new Button(I18N.get("process.refresh"));

        HBox controlsRight = new HBox(
            10, cpuScopeBox, refreshIntervalLabel, refreshIntervalBox, autoRefreshCheckBox, refreshBtn);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topControls = new HBox(10, sortBox, spacer, controlsRight);

        long totalMemory = systemInfoService.getMemory().getTotal();
        int logicalProcessorCount = systemInfoService.getProcessor().getLogicalProcessorCount();
        Map<Integer, OSProcess> previousProcesses = new HashMap<>();
        AtomicBoolean refreshing = new AtomicBoolean(false);
        AtomicLong lastAutoRefreshTime = new AtomicLong(0L);

        Runnable refreshAction = () -> refreshProcesses(
            os,
            summaryGrid,
            processTable,
            totalMemory,
            logicalProcessorCount,
            previousProcesses,
            selectedValue(sortGroup, ProcessSort.CPU),
            selectedValue(cpuScopeGroup, CpuPercentScope.SYSTEM),
            refreshing);

        refreshBtn.setOnAction(e -> refreshAction.run());
        sortGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> refreshAction.run());
        cpuScopeGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> refreshAction.run());
        refreshIntervalBox.valueProperty().addListener((obs, oldV, newV) -> lastAutoRefreshTime.set(0L));

        refreshAction.run();
        scheduler.scheduleAtFixedRate(() -> {
            if (!autoRefreshCheckBox.isSelected()) {
                return;
            }
            int refreshInterval = getRefreshIntervalSeconds(refreshIntervalBox.getValue());
            long now = System.nanoTime();
            long minElapsedNanos = Math.round(refreshInterval * NANOS_PER_SECOND);
            long previousRefreshTime = lastAutoRefreshTime.get();
            if (now - previousRefreshTime < minElapsedNanos) {
                return;
            }
            if (!lastAutoRefreshTime.compareAndSet(previousRefreshTime, now)) {
                return;
            }
            Platform.runLater(refreshAction);
        }, 1, 1, TimeUnit.SECONDS);

        content.getChildren().addAll(topControls, processTable);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    private void refreshProcesses(OperatingSystem os,
                                  GridPane summaryGrid,
                                  TableView<ProcessRow> processTable,
                                  long totalMemory,
                                  int logicalProcessorCount,
                                  Map<Integer, OSProcess> previousProcesses,
                                  ProcessSort processSort,
                                  CpuPercentScope cpuPercentScope,
                                  AtomicBoolean refreshing) {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        scheduler.execute(() -> {
            try {
                int processCount = os.getProcessCount();
                int processLimit = resolveProcessLimit(processCount);
                List<OSProcess> processes = os.getProcesses(
                    OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                    OperatingSystem.ProcessSorting.NO_SORTING,
                    processLimit);

                List<ProcessRow> rows = new ArrayList<>(processes.size());
                Map<Integer, OSProcess> currentProcessMap = new HashMap<>(processes.size());
                Map<String, Image> iconCache = new HashMap<>();
                for (OSProcess process : processes) {
                    int pid = process.getProcessID();
                    OSProcess previousProcess = previousProcesses.get(pid);
                    double cpuLoad = previousProcess != null
                        ? process.getProcessCpuLoadBetweenTicks(previousProcess)
                        : process.getProcessCpuLoadCumulative();
                    double cpuPercent = calculateCpuPercent(cpuLoad, cpuPercentScope, logicalProcessorCount);
                    double cumulativeCpuPercent =
                        calculateCpuPercent(process.getProcessCpuLoadCumulative(), cpuPercentScope, logicalProcessorCount);
                    double memoryPercent = calculateMemoryPercent(process.getResidentSetSize(), totalMemory);

                    String iconKey = createProcessIconCacheKey(process.getPath(), process.getName());
                    Image processIcon = iconCache.computeIfAbsent(iconKey, key -> loadProcessIcon(process.getPath()));

                    rows.add(new ProcessRow(
                        processIcon,
                        pid,
                        process.getParentProcessID(),
                        process.getThreadCount(),
                        cpuPercent,
                        cumulativeCpuPercent,
                        memoryPercent,
                        SystemInfoService.formatBytes(process.getVirtualSize()),
                        SystemInfoService.formatBytes(process.getResidentSetSize()),
                        process.getName()));
                    currentProcessMap.put(pid, process);
                }

                rows.sort(createSortComparator(processSort));
                previousProcesses.keySet().retainAll(currentProcessMap.keySet());
                previousProcesses.putAll(currentProcessMap);

                int threadCount = os.getThreadCount();
                Platform.runLater(() -> {
                    summaryGrid.getChildren().clear();
                    addGridRow(summaryGrid, 0, I18N.get("process.count"), String.valueOf(processCount));
                    addGridRow(summaryGrid, 1, I18N.get("process.threadCount"), String.valueOf(threadCount));
                    processTable.getItems().setAll(rows);
                });
            } finally {
                refreshing.set(false);
            }
        });
    }

    static String formatPercentValue(double value) {
        return String.format("%.1f%%", value);
    }

    static int resolveProcessLimit(int processCount) {
        return Math.max(processCount, 1);
    }

    static int getRefreshIntervalSeconds(Integer refreshIntervalSeconds) {
        if (refreshIntervalSeconds == null || refreshIntervalSeconds <= 0) {
            return DEFAULT_REFRESH_INTERVAL_SECONDS;
        }
        return refreshIntervalSeconds;
    }

    static double calculateCpuPercent(double cpuLoad, CpuPercentScope scope, int logicalProcessorCount) {
        if (!Double.isFinite(cpuLoad) || cpuLoad < 0) {
            return 0d;
        }
        double cpuPercent = cpuLoad * 100d;
        if (scope == CpuPercentScope.SYSTEM) {
            cpuPercent /= Math.max(logicalProcessorCount, 1);
            return Math.min(cpuPercent, 100d);
        }
        return Math.min(cpuPercent, 100d);
    }

    static double calculateMemoryPercent(long residentSetSize, long totalMemory) {
        if (residentSetSize <= 0 || totalMemory <= 0) {
            return 0d;
        }
        return Math.min((residentSetSize * 100d) / totalMemory, 100d);
    }

    static Comparator<ProcessRow> createSortComparator(ProcessSort processSort) {
        Comparator<ProcessRow> comparator = switch (processSort) {
            case CPU -> Comparator.comparingDouble(ProcessRow::cpuPercent).reversed();
            case CUMULATIVE_CPU -> Comparator.comparingDouble(ProcessRow::cumulativeCpuPercent).reversed();
            case MEMORY -> Comparator.comparingDouble(ProcessRow::memoryPercent).reversed();
        };
        return comparator.thenComparingLong(ProcessRow::pid);
    }

    static String createProcessIconCacheKey(String processPath, String processName) {
        if (processPath != null) {
            String normalizedPath = processPath.trim();
            if (!normalizedPath.isEmpty()) {
                return normalizedPath;
            }
        }
        return processName == null ? "" : processName;
    }

    static Image loadProcessIcon(String processPath) {
        if (processPath == null || processPath.isBlank()) {
            return null;
        }
        try {
            File processFile = new File(processPath);
            if (!processFile.exists()) {
                return null;
            }
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(processFile);
            if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                return null;
            }
            BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            icon.paintIcon(null, graphics, 0, 0);
            graphics.dispose();
            return SwingFXUtils.toFXImage(image, null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T selectedValue(ToggleGroup toggleGroup, T defaultValue) {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle == null || selectedToggle.getUserData() == null) {
            return defaultValue;
        }
        return (T) selectedToggle.getUserData();
    }

    private <T> RadioButton createRadioButton(String text, T userData, ToggleGroup toggleGroup) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.setToggleGroup(toggleGroup);
        radioButton.setUserData(userData);
        return radioButton;
    }

    enum ProcessSort {
        CPU,
        CUMULATIVE_CPU,
        MEMORY
    }

    enum CpuPercentScope {
        ONE_PROCESSOR,
        SYSTEM
    }

    record ProcessRow(Image icon,
                      long pid,
                      long ppid,
                      long threads,
                      double cpuPercent,
                      double cumulativeCpuPercent,
                      double memoryPercent,
                      String virtualMemory,
                      String residentMemory,
                      String name) {
    }
}
