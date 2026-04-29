package com.tlcsdm.insightpc.config;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConfigurationTest {

    @Test
    void logbackSuppressesWindowsWmiThermalWarnings() throws IOException {
        String logback = readClasspathResource("/logback.xml");
        assertTrue(hasLoggerWithLevel(logback, "oshi.util.platform.windows.WmiQueryHandler", "ERROR"));
    }

    @Test
    void memoryUsagePercentLabelIsFixedBlack() throws IOException {
        String css = readClasspathResource("/com/tlcsdm/insightpc/style.css");
        assertSelectorHasBlackText(css, ".usage-percent-label");
        assertSelectorHasBlackText(css, ".usage-percent-label.usage-percent-label-low");
    }

    @Test
    void doxygenKeepsInsightPcOverridesFromTemplate() throws IOException {
        Map<String, String> settings = parseDoxygenSettings(readProjectFile("doxygen/Doxyfile"));

        assertEquals("\"InsightPC\"", settings.get("PROJECT_NAME"));
        assertEquals("\"Cross-platform system information visualizer built with JavaFX and OSHI\"",
            settings.get("PROJECT_BRIEF"));
        assertEquals("src/main/java", settings.get("STRIP_FROM_PATH"));
        assertEquals("README.md\ndoxygen/packages.dox\ndoxygen/pages\nsrc/main/java", settings.get("INPUT"));
        assertEquals("target\nbuild\ndocs-gen\nsrc/test\nreadme\n.git", settings.get("EXCLUDE"));
        assertEquals("readme doxygen/pages", settings.get("IMAGE_PATH"));
        assertEquals("0", settings.get("TOC_INCLUDE_HEADINGS"));
        assertEquals("LIGHT", settings.get("HTML_COLORSTYLE"));
        assertEquals("doxygen/custom.js\nLICENSE", settings.get("HTML_EXTRA_FILES"));
    }

    @Test
    void doxygenSupportsPlantUmlConfiguration() throws IOException {
        Map<String, String> settings = parseDoxygenSettings(readProjectFile("doxygen/Doxyfile"));

        assertEquals("YES", settings.get("HAVE_DOT"));
        assertEquals("svg", settings.get("DOT_IMAGE_FORMAT"));
        assertEquals("YES", settings.get("INTERACTIVE_SVG"));
        assertEquals("$(PLANTUML_JAR_PATH)", settings.get("PLANTUML_JAR_PATH"));
    }

    private static String readProjectFile(String path) throws IOException {
        return Files.readString(Path.of(System.getProperty("user.dir")).resolve(path), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseDoxygenSettings(String text) {
        Map<String, String> settings = new LinkedHashMap<>();
        String currentKey = null;
        StringBuilder currentValue = new StringBuilder();

        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            if (currentKey == null) {
                int equalsIndex = line.indexOf('=');
                if (equalsIndex < 0) {
                    continue;
                }
                currentKey = line.substring(0, equalsIndex).trim();
                appendDoxygenValue(currentValue, line.substring(equalsIndex + 1).trim(), false);
                if (!trimmed.endsWith("\\")) {
                    settings.put(currentKey, currentValue.toString());
                    currentKey = null;
                    currentValue.setLength(0);
                }
                continue;
            }

            appendDoxygenValue(currentValue, trimmed, true);
            if (!trimmed.endsWith("\\")) {
                settings.put(currentKey, currentValue.toString());
                currentKey = null;
                currentValue.setLength(0);
            }
        }

        return settings;
    }

    private static void appendDoxygenValue(StringBuilder value, String rawLine, boolean continuation) {
        String normalized = rawLine.endsWith("\\")
            ? rawLine.substring(0, rawLine.length() - 1).trim()
            : rawLine;
        if (value.length() > 0 && continuation) {
            value.append('\n');
        }
        value.append(normalized);
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
        int selectorIndex = css.indexOf(selector);
        assertTrue(selectorIndex >= 0);
        int blockStart = css.indexOf('{', selectorIndex);
        assertTrue(blockStart >= 0);
        int blockEnd = css.indexOf('}', blockStart + 1);
        assertTrue(blockEnd >= 0);
        String block = css.substring(blockStart + 1, blockEnd).replaceAll("\\s+", "");
        assertTrue(block.contains("-fx-text-fill:black;"));
    }

    private static boolean hasLoggerWithLevel(String xml, String loggerName, String level) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList loggerNodes = document.getElementsByTagName("logger");
            for (int i = 0; i < loggerNodes.getLength(); i++) {
                var loggerNode = loggerNodes.item(i);
                var attributes = loggerNode.getAttributes();
                if (attributes == null) {
                    continue;
                }
                var nameNode = attributes.getNamedItem("name");
                var levelNode = attributes.getNamedItem("level");
                if (nameNode != null && levelNode != null
                    && loggerName.equals(nameNode.getNodeValue())
                    && level.equals(levelNode.getNodeValue())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
