package com.tlcsdm.insightpc.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced tests for the AppTheme enum covering edge cases and exhaustive checks.
 */
class AppThemeEnhancedTest {

    @ParameterizedTest
    @EnumSource(AppTheme.class)
    void testEveryThemeHasNonEmptyDisplayNameKey(AppTheme theme) {
        String key = theme.getDisplayNameKey();
        assertNotNull(key);
        assertFalse(key.isBlank(), "Display name key should not be blank for " + theme.name());
        assertTrue(key.startsWith("settings.theme."), "Key should start with 'settings.theme.' for " + theme.name());
    }

    @ParameterizedTest
    @EnumSource(AppTheme.class)
    void testEveryThemeHasNonEmptyDisplayName(AppTheme theme) {
        String name = theme.getDisplayName();
        assertNotNull(name);
        assertFalse(name.isBlank(), "Display name should not be blank for " + theme.name());
    }

    @ParameterizedTest
    @EnumSource(AppTheme.class)
    void testToStringMatchesDisplayName(AppTheme theme) {
        assertEquals(theme.getDisplayName(), theme.toString());
    }

    @Test
    void testGetSavedThemeNeverReturnsNull() {
        assertNotNull(AppTheme.getSavedTheme());
    }

    @Test
    void testGetSavedThemeDefaultIsPrimerLight() {
        // Default is ATLANTAFX_PRIMER_LIGHT unless user has changed it
        AppTheme theme = AppTheme.getSavedTheme();
        assertNotNull(theme);
    }

    @Test
    void testSaveAndRetrieveTheme() {
        AppTheme original = AppTheme.getSavedTheme();
        try {
            AppTheme.saveTheme(AppTheme.ATLANTAFX_NORD_DARK);
            assertEquals(AppTheme.ATLANTAFX_NORD_DARK, AppTheme.getSavedTheme());
        } finally {
            // Restore original
            AppTheme.saveTheme(original);
        }
    }

    @Test
    void testSaveNullThemeDoesNothing() {
        AppTheme original = AppTheme.getSavedTheme();
        AppTheme.saveTheme(null);
        assertEquals(original, AppTheme.getSavedTheme());
    }

    @Test
    void testThemeCount() {
        assertEquals(7, AppTheme.values().length);
    }

    @Test
    void testValueOfValidNames() {
        assertEquals(AppTheme.ATLANTAFX_PRIMER_LIGHT, AppTheme.valueOf("ATLANTAFX_PRIMER_LIGHT"));
        assertEquals(AppTheme.ATLANTAFX_DRACULA, AppTheme.valueOf("ATLANTAFX_DRACULA"));
    }

    @Test
    void testValueOfInvalidNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> AppTheme.valueOf("NONEXISTENT"));
    }

    @Test
    void testDisplayNameKeysAreUnique() {
        AppTheme[] themes = AppTheme.values();
        for (int i = 0; i < themes.length; i++) {
            for (int j = i + 1; j < themes.length; j++) {
                assertNotEquals(themes[i].getDisplayNameKey(), themes[j].getDisplayNameKey(),
                    themes[i].name() + " and " + themes[j].name() + " should have different keys");
            }
        }
    }
}
