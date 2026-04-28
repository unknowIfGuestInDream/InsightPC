package com.tlcsdm.insightpc;

import com.tlcsdm.insightpc.config.AppSettings;
import com.tlcsdm.insightpc.config.I18N;
import com.tlcsdm.insightpc.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    private static final double RESIZE_MARGIN = 6.0;

    private MainController controller;
    private Cursor resizeCursor = Cursor.DEFAULT;
    private double resizeStartScreenX;
    private double resizeStartScreenY;
    private double resizeStartX;
    private double resizeStartY;
    private double resizeStartWidth;
    private double resizeStartHeight;

    @Override
    public void init() {
        // Apply saved theme before UI is created
        AppSettings.getInstance().applyInitialSettings();
    }

    /**
     * Creates and shows the main application window.
     *
     * @param primaryStage the JavaFX primary stage
     * @throws IOException if the main FXML layout cannot be loaded
     */
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
        enableUndecoratedResize(primaryStage, scene);

        primaryStage.setTitle(I18N.get("app.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
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

    /**
     * Launches InsightPC from an IDE or exploded classpath.
     *
     * @param args command-line arguments passed to JavaFX
     */
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

    private void enableUndecoratedResize(Stage stage, Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (stage.isMaximized()) {
                resizeCursor = Cursor.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
            }
            resizeCursor = resolveResizeCursor(scene, event);
            scene.setCursor(resizeCursor);
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() != MouseButton.PRIMARY || resizeCursor == Cursor.DEFAULT || stage.isMaximized()) {
                return;
            }
            resizeStartScreenX = event.getScreenX();
            resizeStartScreenY = event.getScreenY();
            resizeStartX = stage.getX();
            resizeStartY = stage.getY();
            resizeStartWidth = stage.getWidth();
            resizeStartHeight = stage.getHeight();
            event.consume();
        });

        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getButton() != MouseButton.PRIMARY || resizeCursor == Cursor.DEFAULT || stage.isMaximized()) {
                return;
            }
            resizeStage(stage, event);
            event.consume();
        });
    }

    private Cursor resolveResizeCursor(Scene scene, MouseEvent event) {
        boolean left = event.getSceneX() <= RESIZE_MARGIN;
        boolean right = event.getSceneX() >= scene.getWidth() - RESIZE_MARGIN;
        boolean top = event.getSceneY() <= RESIZE_MARGIN;
        boolean bottom = event.getSceneY() >= scene.getHeight() - RESIZE_MARGIN;

        if (left && top) {
            return Cursor.NW_RESIZE;
        }
        if (left && bottom) {
            return Cursor.SW_RESIZE;
        }
        if (right && top) {
            return Cursor.NE_RESIZE;
        }
        if (right && bottom) {
            return Cursor.SE_RESIZE;
        }
        if (left) {
            return Cursor.W_RESIZE;
        }
        if (right) {
            return Cursor.E_RESIZE;
        }
        if (top) {
            return Cursor.N_RESIZE;
        }
        if (bottom) {
            return Cursor.S_RESIZE;
        }
        return Cursor.DEFAULT;
    }

    private void resizeStage(Stage stage, MouseEvent event) {
        double deltaX = event.getScreenX() - resizeStartScreenX;
        double deltaY = event.getScreenY() - resizeStartScreenY;
        double minWidth = Math.max(stage.getMinWidth(), MIN_WIDTH);
        double minHeight = Math.max(stage.getMinHeight(), MIN_HEIGHT);
        double newX = resizeStartX;
        double newY = resizeStartY;
        double newWidth = resizeStartWidth;
        double newHeight = resizeStartHeight;

        if (isWestResize(resizeCursor)) {
            newWidth = resizeStartWidth - deltaX;
            if (newWidth < minWidth) {
                newWidth = minWidth;
            }
            newX = resizeStartX + (resizeStartWidth - newWidth);
        } else if (isEastResize(resizeCursor)) {
            newWidth = Math.max(minWidth, resizeStartWidth + deltaX);
        }

        if (isNorthResize(resizeCursor)) {
            newHeight = resizeStartHeight - deltaY;
            if (newHeight < minHeight) {
                newHeight = minHeight;
            }
            newY = resizeStartY + (resizeStartHeight - newHeight);
        } else if (isSouthResize(resizeCursor)) {
            newHeight = Math.max(minHeight, resizeStartHeight + deltaY);
        }

        stage.setX(newX);
        stage.setY(newY);
        stage.setWidth(newWidth);
        stage.setHeight(newHeight);
    }

    private boolean isWestResize(Cursor cursor) {
        return cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE;
    }

    private boolean isEastResize(Cursor cursor) {
        return cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE;
    }

    private boolean isNorthResize(Cursor cursor) {
        return cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE;
    }

    private boolean isSouthResize(Cursor cursor) {
        return cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE;
    }
}
