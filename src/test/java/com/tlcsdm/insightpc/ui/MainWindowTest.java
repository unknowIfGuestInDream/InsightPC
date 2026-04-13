package com.tlcsdm.insightpc.ui;

import com.tlcsdm.insightpc.InsightApplication;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFX UI tests for the main application window.
 * Verifies that the main layout, menus, and tabs are rendered correctly.
 */
@ExtendWith(ApplicationExtension.class)
class MainWindowTest {

    private Stage stage;

    @Start
    private void start(Stage stage) throws Exception {
        this.stage = stage;
        InsightApplication app = new InsightApplication();
        app.init();
        app.start(stage);
    }

    @Test
    void testWindowIsShowing(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(stage.isShowing(), "Main window should be visible");
    }

    @Test
    void testWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("InsightPC", stage.getTitle());
    }

    @Test
    void testMenuBarExists(FxRobot robot) {
        MenuBar menuBar = robot.lookup(".menu-bar").queryAs(MenuBar.class);
        assertNotNull(menuBar, "MenuBar should exist");
        assertEquals(2, menuBar.getMenus().size(), "Should have 2 menus (File, Help)");
    }

    @Test
    void testFileMenuItems(FxRobot robot) {
        MenuBar menuBar = robot.lookup(".menu-bar").queryAs(MenuBar.class);
        var fileMenu = menuBar.getMenus().get(0);
        // File menu: Settings, Separator, Restart, Exit
        assertEquals(4, fileMenu.getItems().size(), "File menu should have 4 items");
    }

    @Test
    void testHelpMenuItems(FxRobot robot) {
        MenuBar menuBar = robot.lookup(".menu-bar").queryAs(MenuBar.class);
        var helpMenu = menuBar.getMenus().get(1);
        // Help menu: About
        assertEquals(1, helpMenu.getItems().size(), "Help menu should have 1 item");
    }

    @Test
    void testTabPaneExists(FxRobot robot) {
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        assertNotNull(tabPane, "TabPane should exist");
        assertTrue(tabPane.getTabs().size() >= 8, "Should have at least 8 tabs");
    }

    @Test
    void testOverviewTabIsSelected(FxRobot robot) {
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        assertNotNull(tabPane.getSelectionModel().getSelectedItem());
        // First tab should be selected by default
        assertEquals(0, tabPane.getSelectionModel().getSelectedIndex());
    }

    @Test
    void testMinWindowSize(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(stage.getMinWidth() >= 800, "Min width should be at least 800");
        assertTrue(stage.getMinHeight() >= 600, "Min height should be at least 600");
    }
}
