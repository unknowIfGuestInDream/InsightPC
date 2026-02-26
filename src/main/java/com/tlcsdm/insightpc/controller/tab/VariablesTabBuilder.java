package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.material.Material;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds the Variables tab showing environment variables.
 */
public class VariablesTabBuilder extends AbstractTabBuilder {

    public VariablesTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        super(systemInfoService, scheduler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tab build() {
        Tab tab = new Tab(I18N.get("tab.variables"));
        tab.setClosable(false);
        tab.setGraphic(createTabIcon(Material.CODE));

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        content.getChildren().add(createSectionLabel(I18N.get("variables.info")));

        TableView<Map.Entry<String, String>> envTable = new TableView<>();
        envTable.setPrefHeight(500);

        TableColumn<Map.Entry<String, String>, String> nameCol = new TableColumn<>(I18N.get("variables.name"));
        nameCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getKey()));
        nameCol.setPrefWidth(250);

        TableColumn<Map.Entry<String, String>, String> valueCol = new TableColumn<>(I18N.get("variables.value"));
        valueCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue()));
        valueCol.setPrefWidth(600);

        envTable.getColumns().addAll(nameCol, valueCol);

        Map<String, String> sortedEnv = new TreeMap<>(System.getenv());
        envTable.getItems().addAll(sortedEnv.entrySet());

        content.getChildren().add(envTable);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
}
