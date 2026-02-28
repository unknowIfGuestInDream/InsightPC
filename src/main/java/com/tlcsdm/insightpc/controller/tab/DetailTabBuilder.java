package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Detail tab showing computer system, baseboard, and firmware info.
 */
public class DetailTabBuilder extends AbstractTabBuilder {

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
        return tab;
    }
}
