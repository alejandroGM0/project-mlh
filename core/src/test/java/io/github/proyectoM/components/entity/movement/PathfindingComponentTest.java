package io.github.proyectoM.components.entity.movement;

import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.entity.movement.PathfindingComponent;
import java.util.LinkedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for PathfindingComponent.
 */
class PathfindingComponentTest {

    /**
     * Verifies that set target marks for recalculation when the target moved far enough.
     */
    @Test
    void setTargetMarksForRecalculationWhenTheTargetMovedFarEnough() {
        PathfindingComponent component = new PathfindingComponent();
        component.setTarget(200.0f, 0.0f);
        Assertions.assertTrue(component.needsNewPath);
        Assertions.assertFalse(component.hasValidPath);
    }

    /**
     * Verifies that set path updates the current waypoint and remaining waypoints.
     */
    @Test
    void setPathUpdatesTheCurrentWaypointAndRemainingWaypoints() {
        PathfindingComponent component = new PathfindingComponent();
        LinkedList<Vector2> path = new LinkedList<Vector2>();
        path.offer(new Vector2(10.0f, 20.0f));
        path.offer(new Vector2(30.0f, 40.0f));
        component.setPath(path);
        Assertions.assertTrue(component.hasValidPath);
        Assertions.assertEquals(2, component.getRemainingWaypoints());
        Assertions.assertEquals(10.0f, component.currentWaypoint.x);
        Assertions.assertEquals(20.0f, component.currentWaypoint.y);
    }

    /**
     * Verifies that advance to next waypoint clears the current waypoint at the end.
     */
    @Test
    void advanceToNextWaypointClearsTheCurrentWaypointAtTheEnd() {
        PathfindingComponent component = new PathfindingComponent();
        LinkedList<Vector2> path = new LinkedList<Vector2>();
        path.offer(new Vector2(10.0f, 20.0f));
        component.setPath(path);
        boolean advanced = component.advanceToNextWaypoint();
        Assertions.assertFalse(advanced);
        Assertions.assertTrue(component.reachedDestination);
        Assertions.assertEquals(0.0f, component.currentWaypoint.x);
        Assertions.assertEquals(0.0f, component.currentWaypoint.y);
    }

    /**
     * Verifies that clear path resets waypoint and failure state.
     */
    @Test
    void clearPathResetsWaypointAndFailureState() {
        PathfindingComponent component = new PathfindingComponent();
        LinkedList<Vector2> path = new LinkedList<Vector2>();
        path.offer(new Vector2(10.0f, 20.0f));
        component.setPath(path);
        component.clearPath();
        Assertions.assertFalse(component.hasValidPath);
        Assertions.assertFalse(component.needsNewPath);
        Assertions.assertTrue(component.lastPathCalculationFailed);
        Assertions.assertEquals(0.0f, component.currentWaypoint.x);
        Assertions.assertEquals(0.0f, component.currentWaypoint.y);
    }
}