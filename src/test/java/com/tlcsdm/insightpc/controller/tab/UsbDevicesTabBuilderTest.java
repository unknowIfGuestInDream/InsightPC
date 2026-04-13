package com.tlcsdm.insightpc.controller.tab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UsbDevicesTabBuilderTest {

    @Test
    void testShouldShowRoot() {
        assertFalse(UsbDevicesTabBuilder.shouldShowRoot());
    }
}
