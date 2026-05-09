package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import oshi.hardware.HWDiskStore;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Storage tab showing disk and file system info.
 */
public class StorageTabBuilder extends AbstractTabBuilder {

    public StorageTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.storage"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignH.HARDDISK));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<HWDiskStore> diskStores = systemInfoService.getDiskStores();

        content.getChildren().add(createSectionLabel(I18N.get("storage.info")));

        for (HWDiskStore disk : diskStores) {
            GridPane grid = createInfoGrid();
            int row = 0;
            addGridRow(grid, row++, I18N.get("storage.name"), disk.getName());
            addGridRow(grid, row++, I18N.get("storage.model"), disk.getModel());
            addGridRow(grid, row++, I18N.get("storage.diskType"), formatDiskType(disk.getDiskType()));
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
                long used = Math.max(total - usable, 0);
                long available = Math.max(usable, 0);
                double usage = calculateUsage(used, total);

                ProgressBar bar = new ProgressBar(usage);
                bar.setMaxWidth(Double.MAX_VALUE);
                bar.setPrefHeight(20);
                Label percentLabel = new Label(formatPercentText(usage));
                percentLabel.getStyleClass().add("key-label");
                percentLabel.getStyleClass().add("usage-percent-label");
                percentLabel.setStyle("-fx-text-fill: black;");
                StackPane barPane = new StackPane(bar, percentLabel);
                barPane.setAlignment(Pos.CENTER);
                barPane.setMaxWidth(Double.MAX_VALUE);

                Label usedLabel = new Label(I18N.get("storage.usage.used",
                    SystemInfoService.formatBytes(used),
                    SystemInfoService.formatBytes(total)));
                Label availableLabel = new Label(I18N.get("storage.usage.available",
                    SystemInfoService.formatBytes(available),
                    SystemInfoService.formatBytes(total)));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox infoRow = new HBox(8, usedLabel, spacer, availableLabel);
                infoRow.setAlignment(Pos.CENTER_LEFT);
                VBox usagePanel = new VBox(4, barPane, infoRow);
                usagePanel.setFillWidth(true);
                content.getChildren().add(fsGrid);
                content.getChildren().add(usagePanel);
            } else {
                content.getChildren().add(fsGrid);
            }
            content.getChildren().add(new Separator());
        });

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    @Override
    protected void addGridRow(GridPane grid, int row, String key, String value) {
        addReadOnlyGridRow(grid, row, key, value);
    }
}
