package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariablesTabBuilderTest {

    @Test
    void testResolveTablePrefHeightRespectsMinimum() {
        assertEquals(VariablesTabBuilder.MIN_TABLE_HEIGHT, VariablesTabBuilder.resolveTablePrefHeight(0));
        assertEquals(VariablesTabBuilder.MIN_TABLE_HEIGHT, VariablesTabBuilder.resolveTablePrefHeight(-10));
        assertEquals(300, VariablesTabBuilder.resolveTablePrefHeight(300));
        assertEquals(VariablesTabBuilder.DEFAULT_TABLE_HEIGHT, VariablesTabBuilder.resolveTablePrefHeight(Double.NaN));
    }

    @Test
    void testToSortedStringMapSortsAndConvertsValues() {
        Map<Object, Object> source = new LinkedHashMap<>();
        source.put("b", 2);
        source.put("a", 1);
        source.put(10, true);
        source.put("nullValue", null);
        source.put(null, "nullKey");

        Map<String, String> sorted = VariablesTabBuilder.toSortedStringMap(source);
        assertEquals(3, sorted.size());
        assertEquals(List.of("10", "a", "b"), sorted.keySet().stream().toList());
        assertEquals("true", sorted.get("10"));
        assertEquals("1", sorted.get("a"));
        assertEquals("2", sorted.get("b"));
        assertFalse(sorted.containsKey("nullValue"));
    }

    @Test
    void testGetSortedJavaPropertiesContainsJavaVersion() {
        Map<String, String> properties = VariablesTabBuilder.getSortedJavaProperties();
        assertTrue(properties.containsKey("java.version"));
    }
}
