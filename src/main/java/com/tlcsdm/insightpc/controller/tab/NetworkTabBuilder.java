package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material.Material;
import oshi.hardware.NetworkIF;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Network tab showing network interface info.
 */
public class NetworkTabBuilder extends AbstractTabBuilder {

    public NetworkTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
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
        return tab;
    }
}
