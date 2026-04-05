package io.github.proyectoM.debug;

import io.github.proyectoM.debug.DebugWindowBounds;
import io.github.proyectoM.debug.DebugWindowLayout;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for DebugWindowLayout.
 */
class DebugWindowLayoutTest {
    private static final int WINDOW_MARGIN = 20;
    private static final int WINDOW_PADDING = 10;
    private static final int SCREEN_WIDTH = 300;
    private static final int SCREEN_HEIGHT = 200;

    /**
     * Verifies that rectangles overlap returns true when areas intersect.
     */
    @Test
    void rectanglesOverlapReturnsTrueWhenAreasIntersect() {
        DebugWindowBounds first = DebugWindowBounds.of(10, 10, 50, 50);
        DebugWindowBounds second = DebugWindowBounds.of(40, 40, 30, 30);
        Assertions.assertTrue(DebugWindowLayout.rectanglesOverlap(first, second));
    }

    /**
     * Verifies that rectangles overlap returns false when areas are separated.
     */
    @Test
    void rectanglesOverlapReturnsFalseWhenAreasAreSeparated() {
        DebugWindowBounds first = DebugWindowBounds.of(0, 0, 20, 20);
        DebugWindowBounds second = DebugWindowBounds.of(30, 30, 10, 10);
        Assertions.assertFalse(DebugWindowLayout.rectanglesOverlap(first, second));
    }

    /**
     * Verifies that rectangles overlap returns false when areas only touch edges.
     */
    @Test
    void rectanglesOverlapReturnsFalseWhenAreasOnlyTouchEdges() {
        DebugWindowBounds first = DebugWindowBounds.of(0, 0, 20, 20);
        DebugWindowBounds second = DebugWindowBounds.of(20, 0, 10, 10);
        Assertions.assertFalse(DebugWindowLayout.rectanglesOverlap(first, second));
    }

    /**
     * Verifies that find available bounds returns margin position when there are no windows.
     */
    @Test
    void findAvailableBoundsReturnsMarginPositionWhenThereAreNoWindows() {
        DebugWindowLayout layout = new DebugWindowLayout(20, 10);
        DebugWindowBounds bounds = layout.findAvailableBounds(List.of(), 50, 40, 300, 200);
        Assertions.assertEquals(20, bounds.getX());
        Assertions.assertEquals(20, bounds.getY());
        Assertions.assertEquals(50, bounds.getWidth());
        Assertions.assertEquals(40, bounds.getHeight());
    }

    /**
     * Verifies that find available bounds skips occupied windows.
     */
    @Test
    void findAvailableBoundsSkipsOccupiedWindows() {
        DebugWindowLayout layout = new DebugWindowLayout(20, 10);
        List<DebugWindowBounds> occupiedBounds = List.of(DebugWindowBounds.of(20, 20, 100, 80));
        DebugWindowBounds bounds = layout.findAvailableBounds(occupiedBounds, 50, 40, 300, 200);
        Assertions.assertEquals(120, bounds.getX());
        Assertions.assertEquals(20, bounds.getY());
    }
}