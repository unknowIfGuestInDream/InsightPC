package com.tlcsdm.insightpc.config;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;

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
