package io.github.proyectoM.pathfinding;

import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.pathfinding.JPSPathfinder;
import io.github.proyectoM.pathfinding.NavigationGrid;
import java.util.Iterator;
import java.util.Queue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for JPSPathfinder.
 */
class JPSPathfinderTest {
    private static final float CELL_SIZE = 32.0f;
    private static final float ORIGIN = 0.0f;
    private static final float NO_RADIUS = 0.0f;
    private static final int GRID_SIZE = 6;
    private static final int START_AND_GOAL_X = 2;
    private static final int START_AND_GOAL_Y = 3;
    private final JPSPathfinder pathfinder = new JPSPathfinder();

    /**
     * Verifies that find path returns single waypoint when start matches goal.
     */
    @Test
    void findPathReturnsSingleWaypointWhenStartMatchesGoal() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        Queue path = this.pathfinder.findPath(grid, 2, 3, 2, 3, 0.0f);
        Assertions.assertNotNull(path);
        Assertions.assertEquals(1, path.size());
        Assertions.assertEquals(new Vector2(2.0f, 3.0f), path.peek());
    }

    /**
     * Verifies that find path returns null when goal is blocked.
     */
    @Test
    void findPathReturnsNullWhenGoalIsBlocked() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        grid.temporarilyBlockCell(4, 4);
        Queue path = this.pathfinder.findPath(grid, 0, 0, 4, 4, 0.0f);
        Assertions.assertNull(path);
    }

    /**
     * Verifies that find path returns path across open grid.
     */
    @Test
    void findPathReturnsPathAcrossOpenGrid() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        Queue path = this.pathfinder.findPath(grid, 0, 0, 4, 4, 0.0f);
        Assertions.assertNotNull(path);
        Assertions.assertEquals(new Vector2(0.0f, 0.0f), path.peek());
        Assertions.assertEquals(new Vector2(4.0f, 4.0f), this.last(path));
    }

    private Vector2 last(Queue<Vector2> path) {
        Vector2 lastWaypoint = null;
        Iterator iterator = path.iterator();
        while (iterator.hasNext()) {
            Vector2 waypoint;
            lastWaypoint = waypoint = (Vector2)iterator.next();
        }
        return lastWaypoint;
    }
}