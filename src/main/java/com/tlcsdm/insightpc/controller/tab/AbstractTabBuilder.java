package com.tlcsdm.insightpc.controller.tab;

import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Base class for tab builders providing shared UI helper methods.
 */
public abstract class AbstractTabBuilder {

    private static final String NOT_AVAILABLE_KEY = "power.notAvailable";

    protected final SystemInfoService systemInfoService;
    protected final ScheduledExecutorService scheduler;

    protected AbstractTabBuilder(SystemInfoService systemInfoService, ScheduledExecutorService scheduler) {
        this.systemInfoService = systemInfoService;
        this.scheduler = scheduler;
    }

    /**
     * Build and return the tab.
     */
    public abstract Tab build();

    protected FontIcon createTabIcon(Ikon icon) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        return fontIcon;
    }

    protected Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }

    protected Label createSectionLabel(Ikon icon, String text) {
        Label label = createSectionLabel(text);
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        label.setGraphic(fontIcon);
        label.setGraphicTextGap(8);
        return label;
    }

    protected GridPane createInfoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 0, 5, 10));
        ColumnConstraints keyCol = new ColumnConstraints();
        keyCol.setMinWidth(160);
        keyCol.setPrefWidth(180);
        ColumnConstraints valCol = new ColumnConstraints();
        valCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(keyCol, valCol);
        return grid;
    }

    protected void addGridRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.getStyleClass().add("key-label");
        Label valLabel = new Label(value != null ? value : "N/A");
        valLabel.setWrapText(true);
        grid.add(keyLabel, 0, row);
        grid.add(valLabel, 1, row);
    }

    /**
     * Add a grid row with a read-only TextField value. Used by detail-oriented tabs
     * for selectable/copyable text display.
     */
    protected void addReadOnlyGridRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key + ":");
        keyLabel.getStyleClass().add("key-label");

        TextField valueField = new TextField(normalizeFieldValue(value));
        valueField.setEditable(false);
        valueField.getStyleClass().add("detail-value-field");
        GridPane.setHgrow(valueField, Priority.ALWAYS);

        grid.add(keyLabel, 0, row);
        grid.add(valueField, 1, row);
    }

    protected HBox createOverviewRow(Ikon icon, String label, String value) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("key-label");
        nameLabel.setMinWidth(120);
        nameLabel.setPrefWidth(120);

        Label valueLabel = new Label(value != null ? value : "N/A");
        valueLabel.setWrapText(true);

        HBox row = new HBox(10, fontIcon, nameLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("info-row");
        return row;
    }

    /**
     * Normalize a field value for display: trims whitespace, replaces null/empty with N/A text.
     */
    static String normalizeFieldValue(String value) {
        if (value == null) {
            return I18N.get(NOT_AVAILABLE_KEY);
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? I18N.get(NOT_AVAILABLE_KEY) : trimmed;
    }

    /**
     * Calculate a usage ratio (0.0 to 1.0) from used and total values.
     */
    static double calculateUsage(long used, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.min(Math.max((double) used / total, 0), 1.0);
    }

    /**
     * Format a usage ratio as a percent string (e.g. "75%").
     */
    static String formatPercentText(double usage) {
        return String.format("%.0f%%", usage * 100);
    }
}
