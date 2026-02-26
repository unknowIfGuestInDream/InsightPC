package com.tlcsdm.insightpc;

import com.tlcsdm.insightpc.config.AppSettings;
import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main JavaFX Application for InsightPC - OSHI system information visualizer.
 */
public class InsightApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(InsightApplication.class);

    private MainController controller;

    @Override
    public void init() {
        // Apply saved theme before UI is created
        AppSettings.getInstance().applyInitialSettings();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        loader.setResources(I18N.getBundle());
        Parent root = loader.load();
        controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root, 1000, 700);

        primaryStage.setTitle(I18N.get("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Set application icon
        setStageIcon(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.shutdown();
            }
            Platform.exit();
        });

        primaryStage.show();
        LOG.info("InsightPC application started");
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void setStageIcon(Stage stage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            LOG.warn("Could not set application icon", e);
        }
    }
}
