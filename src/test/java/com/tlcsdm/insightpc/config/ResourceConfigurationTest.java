package com.tlcsdm.insightpc.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConfigurationTest {

    @Test
    void logbackSuppressesWindowsWmiThermalWarnings() throws IOException {
        String logback = readClasspathResource("/logback.xml");
        Pattern loggerPattern = Pattern.compile(
            "<logger\\s+name=\"oshi\\.util\\.platform\\.windows\\.WmiQueryHandler\"\\s+level=\"ERROR\"\\s*/>");
        assertTrue(loggerPattern.matcher(logback).find());
    }

    @Test
    void memoryUsagePercentLabelIsFixedBlack() throws IOException {
        String css = readClasspathResource("/com/tlcsdm/insightpc/style.css");
        assertSelectorHasBlackText(css, ".usage-percent-label");
        assertSelectorHasBlackText(css, ".usage-percent-label.usage-percent-label-low");
    }

    private static String readClasspathResource(String path) throws IOException {
        try (InputStream inputStream = ResourceConfigurationTest.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertSelectorHasBlackText(String css, String selector) {
        Pattern selectorPattern = Pattern.compile(
            Pattern.quote(selector) + "\\s*\\{[^}]*-fx-text-fill\\s*:\\s*black;",
            Pattern.DOTALL);
        assertTrue(selectorPattern.matcher(css).find());
    }
}
