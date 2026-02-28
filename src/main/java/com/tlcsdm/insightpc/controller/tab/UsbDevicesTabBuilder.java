package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignU;
import oshi.hardware.UsbDevice;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the USB Devices tab showing USB device hierarchy.
 */
public class UsbDevicesTabBuilder extends AbstractTabBuilder {

    public UsbDevicesTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.usbDevices"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignU.USB));

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
        return tab;
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
}
