package com.tlcsdm.insightpc.controller;

import com.dlsc.preferencesfx.PreferencesFx;
import com.tlcsdm.insightpc.config.AppSettings;
import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.controller.tab.*;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Main controller for the InsightPC application.
 * Manages the tabbed OSHI information panels by delegating to tab builders.
 */
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
    private static final double SETTINGS_DEFAULT_HEIGHT = 480;
    private static final double SETTINGS_MIN_HEIGHT = 360;
    private static final double SETTINGS_HEIGHT_RATIO_OF_MAIN = 0.7;
    private static final int SETTINGS_WINDOW_MAX_RETRIES = 10;

    @FXML
    private TabPane tabPane;

    private Stage primaryStage;
    private SystemInfoService systemInfoService;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        systemInfoService = new SystemInfoService();
        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "insightpc-refresh");
            t.setDaemon(true);
            return t;
        });

        tabPane.getTabs().addAll(
            new OverviewTabBuilder(systemInfoService, scheduler).build(),
            new DetailTabBuilder(systemInfoService, scheduler).build(),
            new MemoryTabBuilder(systemInfoService, scheduler).build(),
            new CpuTabBuilder(systemInfoService, scheduler).build(),
            new StorageTabBuilder(systemInfoService, scheduler).build(),
            new NetworkTabBuilder(systemInfoService, scheduler).build(),
            new VariablesTabBuilder(systemInfoService, scheduler).build(),
            new ProcessTabBuilder(systemInfoService, scheduler).build(),
            new UsbDevicesTabBuilder(systemInfoService, scheduler).build(),
            new PowerTabBuilder(systemInfoService, scheduler).build()
        );

        LOG.info("All tabs initialized");
    }

    /**
     * Set the primary stage reference.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Open the settings dialog.
     */
    @FXML
    public void openSettings() {
        PreferencesFx preferencesFx = AppSettings.getInstance().getPreferencesFx();
        preferencesFx.getView().setPrefHeight(SETTINGS_DEFAULT_HEIGHT);
        preferencesFx.show(true);
        adjustSettingsWindow(preferencesFx, 0);
    }

    private void adjustSettingsWindow(PreferencesFx preferencesFx, int retryCount) {
        Platform.runLater(() -> {
            if (preferencesFx.getView().getScene() == null || preferencesFx.getView().getScene().getWindow() == null) {
                if (retryCount < SETTINGS_WINDOW_MAX_RETRIES) {
                    adjustSettingsWindow(preferencesFx, retryCount + 1);
                } else {
                    LOG.warn("Could not position preferences dialog after {} attempts", SETTINGS_WINDOW_MAX_RETRIES);
                }
                return;
            }

            Stage settingsStage = (Stage) preferencesFx.getView().getScene().getWindow();
            double targetHeight = SETTINGS_DEFAULT_HEIGHT;
            if (primaryStage != null && primaryStage.isShowing()) {
                targetHeight = Math.max(SETTINGS_MIN_HEIGHT,
                    Math.min(SETTINGS_DEFAULT_HEIGHT, primaryStage.getHeight() * SETTINGS_HEIGHT_RATIO_OF_MAIN));
            }
            settingsStage.setHeight(targetHeight);
            if (primaryStage != null && primaryStage.isShowing()) {
                settingsStage.sizeToScene();
                double targetWidth = settingsStage.getWidth() > 0
                    ? settingsStage.getWidth()
                    : settingsStage.getScene().getWidth();
                settingsStage.setY(primaryStage.getY() + (primaryStage.getHeight() - targetHeight) / 2.0);
                settingsStage.setX(primaryStage.getX() + (primaryStage.getWidth() - targetWidth) / 2.0);
            }
        });
    }

    /**
     * Exit the application.
     */
    @FXML
    public void exitApplication() {
        shutdown();
        if (primaryStage != null) {
            primaryStage.close();
        }
    }

    /**
     * Restart the application.
     */
    @FXML
    public void restartApplication() {
        LOG.info("Application restarting");
        shutdown();
        if (primaryStage != null) {
            primaryStage.close();
        }
        Platform.runLater(() -> {
            try {
                com.tlcsdm.insightpc.InsightApplication app = new com.tlcsdm.insightpc.InsightApplication();
                Stage newStage = new Stage();
                app.init();
                app.start(newStage);
            } catch (Exception e) {
                LOG.error("Failed to restart application", e);
            }
        });
    }

    /**
     * Show about dialog.
     */
    @FXML
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18N.get("menu.about"));
        alert.setHeaderText(I18N.get("app.title"));

        Label descriptionLabel = new Label(I18N.get("about.description"));
        descriptionLabel.setWrapText(true);
        Hyperlink openSourceLink = new Hyperlink(I18N.get("about.openSource"));
        openSourceLink.setOnAction(e -> showOpenSourceLibrariesDialog());
        VBox content = new VBox(8, descriptionLabel, openSourceLink);
        content.setPadding(new Insets(4, 0, 0, 12));
        alert.getDialogPane().setContent(content);

        Image logoImage = new Image(getClass().getResourceAsStream("/com/tlcsdm/insightpc/logo.png"));
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(64);
        logoView.setFitHeight(64);
        logoView.setPreserveRatio(true);
        alert.setGraphic(logoView);
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(logoImage);
        alert.showAndWait();
    }

    /**
     * Show open-source libraries dialog.
     */
    private void showOpenSourceLibrariesDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(I18N.get("about.openSource"));
        dialog.setHeaderText(I18N.get("about.openSource.description"));
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }

        TableView<OpenSourceLibrary> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setItems(FXCollections.observableArrayList(
            new OpenSourceLibrary("JavaFX", "GPL v2 with Classpath Exception"),
            new OpenSourceLibrary("ControlsFX", "BSD 3-Clause License"),
            new OpenSourceLibrary("PreferencesFX", "Apache License 2.0"),
            new OpenSourceLibrary("AtlantaFX", "MIT License"),
            new OpenSourceLibrary("Ikonli", "Apache License 2.0"),
            new OpenSourceLibrary("OSHI", "MIT License"),
            new OpenSourceLibrary("JNA", "Apache License 2.0 / LGPL 2.1"),
            new OpenSourceLibrary("SLF4J", "MIT License"),
            new OpenSourceLibrary("Logback", "EPL 1.0 / LGPL 2.1"),
            new OpenSourceLibrary("Gson", "Apache License 2.0")
        ));

        TableColumn<OpenSourceLibrary, String> nameColumn = new TableColumn<>(I18N.get("about.openSource.table.library"));
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().name()));

        TableColumn<OpenSourceLibrary, String> licenseColumn = new TableColumn<>(I18N.get("about.openSource.table.license"));
        licenseColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().license()));

        tableView.getColumns().addAll(nameColumn, licenseColumn);

        VBox content = new VBox(8, tableView);
        content.setPadding(new Insets(10));
        content.setPrefSize(500, 320);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/com/tlcsdm/insightpc/logo.png"));
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(logoImage);
        } catch (Exception e) {
            LOG.warn("Could not set dialog icon", e);
        }

        dialog.showAndWait();
    }

    private record OpenSourceLibrary(String name, String license) {
    }

    /**
     * Shutdown and cleanup resources.
     */
    public void shutdown() {
        LOG.info("Application shutting down");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
