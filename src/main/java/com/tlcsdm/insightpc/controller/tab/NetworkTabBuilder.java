package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import oshi.hardware.NetworkIF;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Builds the Network tab showing network interface info.
 */
public class NetworkTabBuilder extends AbstractTabBuilder {

    private static final long ONE_SECOND_IN_NANOS = 1_000_000_000L;
    private static final String READONLY_VALUE_FIELD_STYLE =
        "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;";

    public NetworkTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.network"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignW.WEB));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<NetworkIF> networkIFs = systemInfoService.getNetworkInterfaces();
        OperatingSystem operatingSystem = systemInfoService.getOperatingSystem();
        NetworkParams networkParams = operatingSystem.getNetworkParams();

        Label downloadSpeedLabel = createSpeedLabel("↓: 0 B/s");
        Label uploadSpeedLabel = createSpeedLabel("↑: 0 B/s");
        HBox speedBox = new HBox(20, downloadSpeedLabel, uploadSpeedLabel);
        content.getChildren().add(speedBox);

        GridPane networkInfoGrid = createInfoGrid();
        int infoRow = 0;
        addReadOnlyValueRow(networkInfoGrid, infoRow++, I18N.get("detail.domainName"), networkParams.getDomainName());
        addReadOnlyValueRow(networkInfoGrid, infoRow++, I18N.get("detail.hostName"), networkParams.getHostName());
        addReadOnlyValueRow(networkInfoGrid, infoRow++, I18N.get("detail.ipv4DefaultGateway"), networkParams.getIpv4DefaultGateway());
        addReadOnlyValueRow(networkInfoGrid, infoRow++, I18N.get("detail.ipv6DefaultGateway"), networkParams.getIpv6DefaultGateway());
        addReadOnlyValueRow(networkInfoGrid, infoRow, I18N.get("detail.dnsServers"), Arrays.toString(networkParams.getDnsServers()));
        content.getChildren().add(networkInfoGrid);

        content.getChildren().add(createSectionLabel(I18N.get("network.info")));

        TableView<NetworkRow> networkTable = new TableView<>();
        networkTable.setPrefHeight(480);

        TableColumn<NetworkRow, String> nameCol = createColumn(I18N.get("network.name"), NetworkRow::name, 120);
        TableColumn<NetworkRow, String> displayNameCol = createColumn(I18N.get("network.displayName"), NetworkRow::displayName, 180);
        TableColumn<NetworkRow, String> aliasCol = createColumn(I18N.get("detail.ifAlias"), NetworkRow::ifAlias, 120);
        TableColumn<NetworkRow, String> macCol = createColumn(I18N.get("network.mac"), NetworkRow::mac, 140);
        TableColumn<NetworkRow, String> mtuCol = createColumn(I18N.get("detail.mtu"), NetworkRow::mtu, 70);
        TableColumn<NetworkRow, String> speedCol = createColumn(I18N.get("network.speed"), NetworkRow::speed, 90);
        TableColumn<NetworkRow, String> ipv4Col = createColumn(I18N.get("network.ipv4"), NetworkRow::ipv4, 160);
        TableColumn<NetworkRow, String> ipv6Col = createColumn(I18N.get("network.ipv6"), NetworkRow::ipv6, 200);
        TableColumn<NetworkRow, String> recvBytesCol = createColumn(I18N.get("network.bytesRecv"), NetworkRow::bytesRecv, 120);
        TableColumn<NetworkRow, String> sentBytesCol = createColumn(I18N.get("network.bytesSent"), NetworkRow::bytesSent, 120);
        TableColumn<NetworkRow, String> recvPacketsCol = createColumn(I18N.get("detail.received"), NetworkRow::packetsRecv, 100);
        TableColumn<NetworkRow, String> sentPacketsCol = createColumn(I18N.get("detail.transmitted"), NetworkRow::packetsSent, 100);
        TableColumn<NetworkRow, String> inErrorCol = createColumn("↓ " + I18N.get("detail.err"), NetworkRow::inErrors, 80);
        TableColumn<NetworkRow, String> outErrorCol = createColumn("↑ " + I18N.get("detail.err"), NetworkRow::outErrors, 80);
        TableColumn<NetworkRow, String> dropCol = createColumn(I18N.get("detail.drop"), NetworkRow::inDrops, 80);
        TableColumn<NetworkRow, String> collisionCol = createColumn(I18N.get("detail.coll"), NetworkRow::collisions, 80);

        networkTable.getColumns().addAll(
            nameCol,
            displayNameCol,
            aliasCol,
            macCol,
            mtuCol,
            speedCol,
            ipv4Col,
            ipv6Col,
            recvBytesCol,
            sentBytesCol,
            recvPacketsCol,
            sentPacketsCol,
            inErrorCol,
            outErrorCol,
            dropCol,
            collisionCol
        );

        content.getChildren().add(networkTable);

        Map<String, Long> previousReceivedBytes = new HashMap<>();
        Map<String, Long> previousSentBytes = new HashMap<>();
        final long[] previousSampleTimeNanos = {System.nanoTime()};

        for (NetworkIF net : networkIFs) {
            net.updateAttributes();
            previousReceivedBytes.put(net.getName(), net.getBytesRecv());
            previousSentBytes.put(net.getName(), net.getBytesSent());
        }

        Runnable refreshTask = () -> {
            long currentSampleTimeNanos = System.nanoTime();
            double elapsedSeconds = Math.max(
                (currentSampleTimeNanos - previousSampleTimeNanos[0]) / (double) ONE_SECOND_IN_NANOS,
                1e-6);
            previousSampleTimeNanos[0] = currentSampleTimeNanos;

            long totalReceivedDelta = 0;
            long totalSentDelta = 0;
            List<NetworkRow> rows = new ArrayList<>(networkIFs.size());

            for (NetworkIF net : networkIFs) {
                net.updateAttributes();
                long currentReceivedBytes = net.getBytesRecv();
                long currentSentBytes = net.getBytesSent();
                long receivedDelta = safeDelta(currentReceivedBytes,
                    previousReceivedBytes.getOrDefault(net.getName(), currentReceivedBytes));
                long sentDelta = safeDelta(currentSentBytes,
                    previousSentBytes.getOrDefault(net.getName(), currentSentBytes));
                previousReceivedBytes.put(net.getName(), currentReceivedBytes);
                previousSentBytes.put(net.getName(), currentSentBytes);
                totalReceivedDelta += receivedDelta;
                totalSentDelta += sentDelta;
                rows.add(NetworkRow.from(net));
            }

            String downloadSpeedText = "↓: " + formatSpeedText(totalReceivedDelta, elapsedSeconds);
            String uploadSpeedText = "↑: " + formatSpeedText(totalSentDelta, elapsedSeconds);

            Platform.runLater(() -> {
                downloadSpeedLabel.setText(downloadSpeedText);
                uploadSpeedLabel.setText(uploadSpeedText);
                networkTable.getItems().setAll(rows);
            });
        };

        refreshTask.run();
        scheduler.scheduleAtFixedRate(refreshTask, 1, 1, TimeUnit.SECONDS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    static long safeDelta(long currentValue, long previousValue) {
        return Math.max(currentValue - previousValue, 0);
    }

    static String formatSpeedText(long bytesDelta, double elapsedSeconds) {
        if (bytesDelta <= 0 || elapsedSeconds <= 0) {
            return "0 B/s";
        }
        long bytesPerSecond = Math.max(Math.round(bytesDelta / elapsedSeconds), 0);
        return SystemInfoService.formatBytes(bytesPerSecond) + "/s";
    }

    static String joinAddressArray(String[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return "N/A";
        }
        return String.join(", ", addresses);
    }

    static String toText(long value) {
        return value >= 0 ? Long.toString(value) : "N/A";
    }

    private Label createSpeedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        return label;
    }

    private TableColumn<NetworkRow, String> createColumn(String title,
                                                          java.util.function.Function<NetworkRow, String> mapper,
                                                          double width) {
        TableColumn<NetworkRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();
            {
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setShowDuration(Duration.seconds(30));
                Tooltip.install(this, tooltip);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    tooltip.setText("");
                } else {
                    setText(item);
                    tooltip.setText(item);
                }
            }
        });
        column.setPrefWidth(width);
        return column;
    }

    private void addReadOnlyValueRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.getStyleClass().add("key-label");
        TextField valueField = new TextField(normalizeFieldValue(value));
        valueField.setEditable(false);
        valueField.setFocusTraversable(false);
        valueField.setStyle(READONLY_VALUE_FIELD_STYLE);
        GridPane.setHgrow(valueField, Priority.ALWAYS);
        grid.add(keyLabel, 0, row);
        grid.add(valueField, 1, row);
    }

    static String normalizeFieldValue(String value) {
        if (value == null) {
            return "N/A";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "N/A" : trimmed;
    }

    private record NetworkRow(
        String name,
        String displayName,
        String ifAlias,
        String mac,
        String mtu,
        String speed,
        String ipv4,
        String ipv6,
        String bytesRecv,
        String bytesSent,
        String packetsRecv,
        String packetsSent,
        String inErrors,
        String outErrors,
        String inDrops,
        String collisions
    ) {
        static NetworkRow from(NetworkIF net) {
            return new NetworkRow(
                net.getName(),
                net.getDisplayName(),
                net.getIfAlias(),
                net.getMacaddr(),
                net.getMTU() > 0 ? Long.toString(net.getMTU()) : "N/A",
                net.getSpeed() > 0 ? SystemInfoService.formatBytes(net.getSpeed() / 8) + "/s" : "N/A",
                joinAddressArray(net.getIPv4addr()),
                joinAddressArray(net.getIPv6addr()),
                SystemInfoService.formatBytes(net.getBytesRecv()),
                SystemInfoService.formatBytes(net.getBytesSent()),
                toText(net.getPacketsRecv()),
                toText(net.getPacketsSent()),
                toText(net.getInErrors()),
                toText(net.getOutErrors()),
                toText(net.getInDrops()),
                toText(net.getCollisions())
            );
        }
    }
}
