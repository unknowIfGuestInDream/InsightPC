package com.tlcsdm.insightpc.model;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced tests for DisplayLocale covering equality, hash codes, and edge cases.
 */
class DisplayLocaleEnhancedTest {

    @Test
    void testConstructorWithValidLocale() {
        DisplayLocale dl = new DisplayLocale(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, dl.getLocale());
    }

    @Test
    void testConstructorWithNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> new DisplayLocale(null));
    }

    @Test
    void testToStringKnownLocales() {
        assertEquals("English", new DisplayLocale(Locale.ENGLISH).toString());
        assertEquals("\u4e2d\u6587", new DisplayLocale(Locale.SIMPLIFIED_CHINESE).toString());
        assertEquals("\u65e5\u672c\u8a9e", new DisplayLocale(Locale.JAPANESE).toString());
    }

    @Test
    void testToStringUnknownLocaleFallsBack() {
        DisplayLocale dl = new DisplayLocale(Locale.FRENCH);
        String result = dl.toString();
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void testEqualsSameObject() {
        DisplayLocale dl = new DisplayLocale(Locale.ENGLISH);
        assertEquals(dl, dl);
    }

    @Test
    void testEqualsSameLanguage() {
        DisplayLocale dl1 = new DisplayLocale(Locale.ENGLISH);
        DisplayLocale dl2 = new DisplayLocale(Locale.ENGLISH);
        assertEquals(dl1, dl2);
    }

    @Test
    void testEqualsDifferentLanguage() {
        DisplayLocale dl1 = new DisplayLocale(Locale.ENGLISH);
        DisplayLocale dl2 = new DisplayLocale(Locale.JAPANESE);
        assertNotEquals(dl1, dl2);
    }

    @Test
    void testEqualsNull() {
        DisplayLocale dl = new DisplayLocale(Locale.ENGLISH);
        assertNotEquals(null, dl);
    }

    @Test
    void testEqualsDifferentType() {
        DisplayLocale dl = new DisplayLocale(Locale.ENGLISH);
        assertNotEquals("English", dl);
    }

    @Test
    void testEqualsSymmetric() {
        DisplayLocale dl1 = new DisplayLocale(Locale.JAPANESE);
        DisplayLocale dl2 = new DisplayLocale(Locale.JAPANESE);
        assertEquals(dl1, dl2);
        assertEquals(dl2, dl1);
    }

    @Test
    void testHashCodeSameLanguage() {
        DisplayLocale dl1 = new DisplayLocale(Locale.ENGLISH);
        DisplayLocale dl2 = new DisplayLocale(Locale.ENGLISH);
        assertEquals(dl1.hashCode(), dl2.hashCode());
    }

    @Test
    void testHashCodeDifferentLanguage() {
        DisplayLocale dl1 = new DisplayLocale(Locale.ENGLISH);
        DisplayLocale dl2 = new DisplayLocale(Locale.JAPANESE);
        // Different languages should generally have different hash codes
        assertNotEquals(dl1.hashCode(), dl2.hashCode());
    }

    @Test
    void testLocaleVariantsWithSameLanguageAreEqual() {
        // Locale.SIMPLIFIED_CHINESE ("zh") and Locale.CHINESE ("zh") share language
        DisplayLocale dl1 = new DisplayLocale(Locale.SIMPLIFIED_CHINESE);
        DisplayLocale dl2 = new DisplayLocale(Locale.CHINESE);
        assertEquals(dl1, dl2, "Same language should be equal regardless of country variant");
    }
}
