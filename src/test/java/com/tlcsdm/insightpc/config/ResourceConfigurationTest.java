package com.tlcsdm.insightpc.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConfigurationTest {

    @Test
    void logbackSuppressesWindowsWmiThermalWarnings() throws IOException {
        String logback = readClasspathResource("/logback.xml");
        assertTrue(logback.contains("oshi.util.platform.windows.WmiQueryHandler"));
        assertTrue(logback.contains("level=\"ERROR\""));
    }

    @Test
    void memoryUsagePercentLabelIsFixedBlack() throws IOException {
        String css = readClasspathResource("/com/tlcsdm/insightpc/style.css");
        assertTrue(css.contains(".usage-percent-label {\n    -fx-text-fill: black;"));
        assertTrue(css.contains(".usage-percent-label.usage-percent-label-low {\n    -fx-text-fill: black;"));
    }

    private static String readClasspathResource(String path) throws IOException {
        try (InputStream inputStream = ResourceConfigurationTest.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
