package io.github.proyectoM.debug;

import io.github.proyectoM.debug.PerformancePanel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for PerformancePanel.
 */
class PerformancePanelTest {

    /**
     * Verifies that format fps uses stable debug label.
     */
    @Test
    void formatFpsUsesStableDebugLabel() {
        Assertions.assertEquals("FPS: 144", PerformancePanel.formatFps(144));
    }

    /**
     * Verifies that format delta seconds uses four decimal places.
     */
    @Test
    void formatDeltaSecondsUsesFourDecimalPlaces() {
        Assertions.assertEquals("Delta: 0.0167", PerformancePanel.formatDeltaSeconds(0.016666668f));
    }

    /**
     * Verifies that format memory usage megabytes uses expected layout.
     */
    @Test
    void formatMemoryUsageMegabytesUsesExpectedLayout() {
        Assertions.assertEquals("Mem: 256 / 1024 MB", PerformancePanel.formatMemoryUsageMegabytes(256L, 1024L));
    }
}