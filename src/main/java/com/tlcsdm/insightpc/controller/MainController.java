package com.tlcsdm.insightpc.controller;

import com.tlcsdm.insightpc.config.AppSettings;
import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.controller.tab.*;
import com.tlcsdm.insightpc.service.SystemInfoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        AppSettings.getInstance().getPreferencesFx().show(true);
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
     * Show about dialog.
     */
    @FXML
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18N.get("menu.about"));
        alert.setHeaderText(I18N.get("app.title"));
        alert.setContentText(I18N.get("about.description"));
        Image logoImage = new Image(getClass().getResourceAsStream("/com/tlcsdm/insightpc/logo.png"));
        alert.setGraphic(new ImageView(logoImage));
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(logoImage);
        alert.showAndWait();
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
