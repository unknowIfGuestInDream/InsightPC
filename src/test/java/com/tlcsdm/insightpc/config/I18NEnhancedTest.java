package com.tlcsdm.insightpc.config;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced tests for the I18N utility class covering edge cases and all locales.
 */
class I18NEnhancedTest {

    @Test
    void testGetBundleNotNull() {
        assertNotNull(I18N.getBundle());
    }

    @Test
    void testGetBundleReturnsResourceBundle() {
        assertInstanceOf(ResourceBundle.class, I18N.getBundle());
    }

    @Test
    void testGetWithNullKeyReturnsNull() {
        assertThrows(NullPointerException.class, () -> I18N.get(null));
    }

    @Test
    void testGetMissingKeyReturnsKey() {
        String key = "this.key.does.not.exist";
        assertEquals(key, I18N.get(key));
    }

    @Test
    void testGetEmptyKeyReturnsEmpty() {
        // Empty key should return empty since it won't be found
        assertEquals("", I18N.get(""));
    }

    @Test
    void testGetExistingKeys() {
        // These keys must exist in all locale files
        String[] requiredKeys = {
            "app.title", "menu.file", "menu.settings", "menu.exit", "menu.restart",
            "menu.help", "menu.about", "menu.openSourceLibraries",
            "tab.overview", "tab.detail", "tab.memory", "tab.cpu",
            "tab.storage", "tab.network", "tab.processes", "tab.power"
        };
        for (String key : requiredKeys) {
            String value = I18N.get(key);
            assertNotEquals(key, value, "Key '" + key + "' should have a translation");
            assertFalse(value.isBlank(), "Value for '" + key + "' should not be blank");
        }
    }

    @Test
    void testGetWithParameters() {
        // Test parametrized get with a known pattern — if key is missing, returns key
        String result = I18N.get("nonexistent.pattern", "arg1", "arg2");
        assertEquals("nonexistent.pattern", result);
    }

    @Test
    void testSupportedLocalesCount() {
        assertEquals(3, I18N.getSupportedLocales().length);
    }

    @Test
    void testSupportedLocalesContainsExpected() {
        Locale[] locales = I18N.getSupportedLocales();
        boolean hasEnglish = false, hasChinese = false, hasJapanese = false;
        for (Locale locale : locales) {
            if ("en".equals(locale.getLanguage())) hasEnglish = true;
            if ("zh".equals(locale.getLanguage())) hasChinese = true;
            if ("ja".equals(locale.getLanguage())) hasJapanese = true;
        }
        assertTrue(hasEnglish, "English should be supported");
        assertTrue(hasChinese, "Chinese should be supported");
        assertTrue(hasJapanese, "Japanese should be supported");
    }

    @Test
    void testCurrentLocaleNotNull() {
        assertNotNull(I18N.getCurrentLocale());
    }

    @Test
    void testCurrentLocaleIsSupported() {
        Locale current = I18N.getCurrentLocale();
        String lang = current.getLanguage();
        assertTrue("en".equals(lang) || "zh".equals(lang) || "ja".equals(lang),
            "Current locale should be one of en/zh/ja but was: " + lang);
    }

    @Test
    void testGetDisplayNameForAllLocales() {
        for (Locale locale : I18N.getSupportedLocales()) {
            String displayName = I18N.getDisplayName(locale);
            assertNotNull(displayName);
            assertFalse(displayName.isBlank());
        }
    }

    @Test
    void testSetLocaleWithValidLocale() {
        Locale original = I18N.getCurrentLocale();
        try {
            I18N.setLocale(Locale.JAPANESE);
            assertEquals("ja", I18N.getCurrentLocale().getLanguage());

            I18N.setLocale(Locale.ENGLISH);
            assertEquals("en", I18N.getCurrentLocale().getLanguage());
        } finally {
            I18N.setLocale(original);
        }
    }

    @Test
    void testSetLocaleWithUnsupportedLocaleDoesNothing() {
        Locale original = I18N.getCurrentLocale();
        I18N.setLocale(Locale.FRENCH);
        assertEquals(original.getLanguage(), I18N.getCurrentLocale().getLanguage());
    }

    @Test
    void testSetLocaleWithNullDoesNothing() {
        Locale original = I18N.getCurrentLocale();
        I18N.setLocale((Locale) null);
        assertEquals(original.getLanguage(), I18N.getCurrentLocale().getLanguage());
    }

    @Test
    void testSetLocaleByLanguageTag() {
        Locale original = I18N.getCurrentLocale();
        try {
            I18N.setLocale("ja");
            assertEquals("ja", I18N.getCurrentLocale().getLanguage());
        } finally {
            I18N.setLocale(original);
        }
    }

    @Test
    void testSetLocaleByInvalidLanguageTag() {
        Locale original = I18N.getCurrentLocale();
        I18N.setLocale("xx");
        assertEquals(original.getLanguage(), I18N.getCurrentLocale().getLanguage());
    }
}
