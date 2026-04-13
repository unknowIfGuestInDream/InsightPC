package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Variables tab showing environment variables.
 */
public class VariablesTabBuilder extends AbstractTabBuilder {
    static final double MIN_TABLE_HEIGHT = 180;
    static final double DEFAULT_TABLE_HEIGHT = 280;
    static final double DEFAULT_DIVIDER_POSITION = 0.5;

    public VariablesTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.variables"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(MaterialDesignC.CODE_BRACES));

        VBox content = new VBox();
        content.setPadding(new Insets(15));
        SplitPane splitPane = new SplitPane(
            createTableSection(I18N.get("variables.systemEnv"), getSortedEnvironmentVariables()),
            createTableSection(I18N.get("variables.javaProps"), getSortedJavaProperties())
        );
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(DEFAULT_DIVIDER_POSITION);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        content.getChildren().add(splitPane);
        tab.setContent(content);
        return tab;
    }

    private VBox createTableSection(String title, Map<String, String> values) {
        VBox section = new VBox(8);
        TableView<Map.Entry<String, String>> table = createVariablesTable(values);
        section.getChildren().addAll(createSectionLabel(title), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return section;
    }

    private TableView<Map.Entry<String, String>> createVariablesTable(Map<String, String> values) {
        TableView<Map.Entry<String, String>> table = new TableView<>();
        table.setMinHeight(MIN_TABLE_HEIGHT);
        table.setPrefHeight(resolveTablePrefHeight(DEFAULT_TABLE_HEIGHT));

        TableColumn<Map.Entry<String, String>, String> nameCol = new TableColumn<>(I18N.get("variables.name"));
        nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        nameCol.setPrefWidth(250);

        TableColumn<Map.Entry<String, String>, String> valueCol = new TableColumn<>(I18N.get("variables.value"));
        valueCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));
        valueCol.setPrefWidth(600);

        table.getColumns().addAll(nameCol, valueCol);
        table.getItems().addAll(values.entrySet());
        return table;
    }

    static double resolveTablePrefHeight(double preferredHeight) {
        if (!Double.isFinite(preferredHeight)) {
            return DEFAULT_TABLE_HEIGHT;
        }
        return Math.max(preferredHeight, MIN_TABLE_HEIGHT);
    }

    static Map<String, String> getSortedEnvironmentVariables() {
        return toSortedStringMap(new HashMap<>(System.getenv()));
    }

    static Map<String, String> getSortedJavaProperties() {
        return toSortedStringMap(new HashMap<>(System.getProperties()));
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
