package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Processes tab showing running processes.
 */
public class ProcessTabBuilder extends AbstractTabBuilder {

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
            new SimpleLongProperty(p.getValue().getProcessID()));
        pidCol.setPrefWidth(70);

        TableColumn<OSProcess, String> nameCol = new TableColumn<>(I18N.get("process.name"));
        nameCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<OSProcess, String> stateCol = new TableColumn<>(I18N.get("process.state"));
        stateCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getState().name()));
        stateCol.setPrefWidth(100);

        TableColumn<OSProcess, String> memCol = new TableColumn<>(I18N.get("process.memory"));
        memCol.setCellValueFactory(p ->
            new SimpleStringProperty(
                SystemInfoService.formatBytes(p.getValue().getResidentSetSize())));
        memCol.setPrefWidth(100);

        TableColumn<OSProcess, String> cpuCol = new TableColumn<>(I18N.get("process.cpuPercent"));
        cpuCol.setCellValueFactory(p ->
            new SimpleStringProperty(
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
        return tab;
    }
}
