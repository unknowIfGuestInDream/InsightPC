package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
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
        return tab;
    }
}
