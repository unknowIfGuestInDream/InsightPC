package com.tlcsdm.fxtemplate.controller;

import com.tlcsdm.fxtemplate.config.AppSettings;
import com.tlcsdm.fxtemplate.config.I18N;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main controller for the template application.
 */
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @FXML
    private Label statusLabel;

    private Stage primaryStage;

    @FXML
    public void initialize() {
        statusLabel.setText(I18N.get("status.ready"));
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(I18N.get("menu.about"));
        alert.setHeaderText(I18N.get("app.title"));
        alert.setContentText(I18N.get("about.description"));
        alert.showAndWait();
    }

    /**
     * Shutdown and cleanup resources.
     */
    public void shutdown() {
        LOG.info("Application shutting down");
    }
}
