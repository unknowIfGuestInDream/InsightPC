package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * Builds the Variables tab showing environment variables.
 */
public class VariablesTabBuilder extends AbstractTabBuilder {
    static final double MIN_TABLE_HEIGHT = 180;
    static final double DEFAULT_TABLE_HEIGHT = 280;
    static final double DEFAULT_DIVIDER_POSITION = 0.5;
    static final int TOOLTIP_WRAP_LINE_LENGTH = 80;

    public VariablesTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(Objects.requireNonNull(systemInfoService, "systemInfoService"),
            Objects.requireNonNull(scheduler, "scheduler"));
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.variables"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignC.CODE_BRACES));

        VBox content = new VBox();
        content.setPadding(new Insets(15));
        SplitPane splitPane = new SplitPane(
            createTableSection(I18N.get("variables.systemEnv"), VariablesTabBuilder::getSortedEnvironmentVariables),
            createTableSection(I18N.get("variables.javaProps"), VariablesTabBuilder::getSortedJavaProperties)
        );
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(DEFAULT_DIVIDER_POSITION);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        content.getChildren().add(splitPane);
        tab.setContent(content);
        return tab;
    }

    private VBox createTableSection(String title, Supplier<Map<String, String>> valuesSupplier) {
        VBox section = new VBox(8);
        TableView<Map.Entry<String, String>> table = createVariablesTable();
        loadTableDataAsync(table, valuesSupplier);
        section.getChildren().addAll(createSectionLabel(title), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return section;
    }

    private TableView<Map.Entry<String, String>> createVariablesTable() {
        TableView<Map.Entry<String, String>> table = new TableView<>();
        table.setMinHeight(MIN_TABLE_HEIGHT);
        table.setPrefHeight(resolveTablePrefHeight(DEFAULT_TABLE_HEIGHT));

        TableColumn<Map.Entry<String, String>, String> nameCol = new TableColumn<>(I18N.get("variables.name"));
        nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        nameCol.setPrefWidth(250);

        TableColumn<Map.Entry<String, String>, String> valueCol = new TableColumn<>(I18N.get("variables.value"));
        valueCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));
        valueCol.setCellFactory(col -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();
            {
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setShowDuration(Duration.seconds(30));
                tooltip.setWrapText(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                String text = resolveTooltipText(empty, item);
                setText(text);
                if (text == null) {
                    setTooltip(null);
                } else {
                    tooltip.setText(wrapTooltipText(text, TOOLTIP_WRAP_LINE_LENGTH));
                    setTooltip(tooltip);
                }
            }
        });
        valueCol.setPrefWidth(600);

        table.getColumns().addAll(nameCol, valueCol);
        return table;
    }

    private void loadTableDataAsync(TableView<Map.Entry<String, String>> table, Supplier<Map<String, String>> valuesSupplier) {
        Runnable loadTask = () -> {
            Map<String, String> values = valuesSupplier.get();
            if (Platform.isFxApplicationThread()) {
                table.getItems().setAll(values.entrySet());
            } else {
                Platform.runLater(() -> table.getItems().setAll(values.entrySet()));
            }
        };
        scheduler.execute(loadTask);
    }

    static String resolveTooltipText(boolean empty, String value) {
        if (empty || value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    static String wrapTooltipText(String text, int lineLength) {
        if (text == null || lineLength < 1) {
            return text;
        }
        StringBuilder wrapped = new StringBuilder(text.length());
        int currentLineLength = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            wrapped.append(ch);
            if (ch == '\n') {
                currentLineLength = 0;
                continue;
            }
            currentLineLength++;
            if (currentLineLength >= lineLength && i < text.length() - 1 && text.charAt(i + 1) != '\n') {
                wrapped.append('\n');
                currentLineLength = 0;
            }
        }
        return wrapped.toString();
    }

    static double resolveTablePrefHeight(double preferredHeight) {
        if (!Double.isFinite(preferredHeight)) {
            return DEFAULT_TABLE_HEIGHT;
        }
        return Math.max(preferredHeight, MIN_TABLE_HEIGHT);
    }

    static Map<String, String> getSortedEnvironmentVariables() {
        return toSortedStringMap(System.getenv());
    }

    static Map<String, String> getSortedJavaProperties() {
        return toSortedStringMap(System.getProperties());
    }

    static Map<String, String> toSortedStringMap(Map<?, ?> source) {
        TreeMap<String, String> sorted = new TreeMap<>();
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                sorted.put(String.valueOf(key), String.valueOf(value));
            }
        });
        return sorted;
    }
}
