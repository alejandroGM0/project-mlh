package io.github.proyectoM.debug;

import com.badlogic.gdx.graphics.Color;
import io.github.proyectoM.debug.EcsMetricsPanel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for EcsMetricsPanel.
 */
class EcsMetricsPanelTest {

    /**
     * Verifies that format system name removes system suffix.
     */
    @Test
    void formatSystemNameRemovesSystemSuffix() {
        Assertions.assertEquals("AnimationSelection", EcsMetricsPanel.formatSystemName((String)"AnimationSelectionSystem"));
    }

    /**
     * Verifies that format system name truncates very long names.
     */
    @Test
    void formatSystemNameTruncatesVeryLongNames() {
        Assertions.assertEquals("VeryLongSystemNam...", EcsMetricsPanel.formatSystemName((String)"VeryLongSystemNameForDebug"));
    }

    /**
     * Verifies that get color for time returns green for fast systems.
     */
    @Test
    void getColorForTimeReturnsGreenForFastSystems() {
        Assertions.assertSame(Color.GREEN, EcsMetricsPanel.getColorForTime(0.2f));
    }

    /**
     * Verifies that get color for time returns yellow for mid range systems.
     */
    @Test
    void getColorForTimeReturnsYellowForMidRangeSystems() {
        Assertions.assertSame(Color.YELLOW, EcsMetricsPanel.getColorForTime(0.5f));
    }

    /**
     * Verifies that get color for time returns red for slow systems.
     */
    @Test
    void getColorForTimeReturnsRedForSlowSystems() {
        Assertions.assertSame(Color.RED, EcsMetricsPanel.getColorForTime(1.2f));
    }
}