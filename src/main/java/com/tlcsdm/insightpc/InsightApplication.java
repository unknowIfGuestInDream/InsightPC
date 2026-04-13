package com.tlcsdm.insightpc;

import com.tlcsdm.insightpc.config.AppSettings;
import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main JavaFX Application for InsightPC - OSHI system information visualizer.
 */
public class InsightApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(InsightApplication.class);

    private static final int PREFERRED_WIDTH = 1000;
    private static final int PREFERRED_HEIGHT = 700;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    private MainController controller;

    @Override
    public void init() {
        // Apply saved theme before UI is created
        AppSettings.getInstance().applyInitialSettings();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            primaryStage.initStyle(StageStyle.UNDECORATED);
        } catch (IllegalStateException e) {
            LOG.debug("Failed to set UNDECORATED style, stage may already be shown: {}", e.getMessage());
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        loader.setResources(I18N.getBundle());
        Parent root = loader.load();
        controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        // Calculate responsive window size based on screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double maxWidth = screenBounds.getWidth() * 0.8;
        double maxHeight = screenBounds.getHeight() * 0.85;
        double initWidth = Math.min(PREFERRED_WIDTH, maxWidth);
        double initHeight = Math.min(PREFERRED_HEIGHT, maxHeight);

        Scene scene = new Scene(root, initWidth, initHeight);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle(I18N.get("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

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
