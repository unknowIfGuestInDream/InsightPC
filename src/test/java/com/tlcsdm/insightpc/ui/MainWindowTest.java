package com.tlcsdm.insightpc.ui;

import com.tlcsdm.insightpc.InsightApplication;
import javafx.application.Application;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFX UI tests for the main application window.
 * Verifies that the main layout, menus, and tabs are rendered correctly.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainWindowTest {

    private static final int USB_TAB_INDEX = 8;
    private Stage stage;
    private Application application;
    private final FxRobot robot = new FxRobot();

    @BeforeAll
    void startApplication() throws Exception {
        stage = FxToolkit.registerPrimaryStage();
        application = FxToolkit.setupApplication(InsightApplication.class);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    void resetUiState() throws Exception {
        FxToolkit.setupFixture(() -> {
            stage.setIconified(false);
            stage.toFront();
            stage.requestFocus();
            TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
            tabPane.getSelectionModel().select(0);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterAll
    void stopApplication() throws Exception {
        FxToolkit.cleanupAfterTest(robot, application);
    }

    @Test
    void testWindowIsShowing() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(stage.isShowing(), "Main window should be visible");
    }

    @Test
    void testWindowTitle() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("InsightPC", stage.getTitle());
    }

    @Test
    void testWindowIsUndecorated() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(StageStyle.UNDECORATED, stage.getStyle(), "Window should use UNDECORATED style");
    }

    @Test
    void testMenuBarExists() {
        MenuBar menuBar = robot.lookup(".menu-bar").queryAs(MenuBar.class);
        assertNotNull(menuBar, "MenuBar should exist");
        assertEquals(2, menuBar.getMenus().size(), "Should have 2 menus (File, Help)");
    }

    @Test
    void testFileMenuItems() {
        MenuBar menuBar = robot.lookup(".menu-bar").queryAs(MenuBar.class);
        var fileMenu = menuBar.getMenus().get(0);
        // File menu: Settings, Separator, Restart, Exit
        assertEquals(4, fileMenu.getItems().size(), "File menu should have 4 items");
    }

    @Test
    void testHelpMenuItems() {
        MenuBar menuBar = robot.lookup(".menu-bar").queryAs(MenuBar.class);
        var helpMenu = menuBar.getMenus().get(1);
        // Help menu: About
        assertEquals(1, helpMenu.getItems().size(), "Help menu should have 1 item");
    }

    @Test
    void testTabPaneExists() {
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        assertNotNull(tabPane, "TabPane should exist");
        assertTrue(tabPane.getTabs().size() >= 8, "Should have at least 8 tabs");
    }

    @Test
    void testOverviewTabIsSelected() {
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        assertNotNull(tabPane.getSelectionModel().getSelectedItem());
        // First tab should be selected by default
        assertEquals(0, tabPane.getSelectionModel().getSelectedIndex());
    }

    @Test
    void testUsbTreeRootIsHidden() {
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        tabPane.getSelectionModel().select(USB_TAB_INDEX);
        WaitForAsyncUtils.waitForFxEvents();

        TreeView<?> usbTree = robot.lookup(".tree-view").queryAs(TreeView.class);
        assertFalse(usbTree.isShowRoot(), "USB tree should hide root node");
    }

    @Test
    void testMinWindowSize() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(stage.getMinWidth() >= 800, "Min width should be at least 800");
        assertTrue(stage.getMinHeight() >= 600, "Min height should be at least 600");
        assertTrue(stage.isResizable(), "Window should be resizable");
    }

    @Test
    void testCustomTitleBarControls() {
        Label title = robot.lookup("#titleBarLabel").queryAs(Label.class);
        Button minimizeButton = robot.lookup("#minimizeButton").queryAs(Button.class);
        Button maximizeButton = robot.lookup("#maximizeButton").queryAs(Button.class);
        Button closeButton = robot.lookup("#closeButton").queryAs(Button.class);

        assertNotNull(title, "Title label should exist");
        assertEquals("InsightPC", title.getText(), "Title label should match window title");
        assertNotNull(minimizeButton, "Minimize button should exist");
        assertNotNull(maximizeButton, "Maximize button should exist");
        assertNotNull(closeButton, "Close button should exist");
        assertFalse(closeButton.getStyleClass().contains("window-close-button"),
            "Close button should not use a special hover style class");
    }
}
