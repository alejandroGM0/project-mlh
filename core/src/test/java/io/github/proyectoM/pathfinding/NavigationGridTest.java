package io.github.proyectoM.pathfinding;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import io.github.proyectoM.pathfinding.NavigationGrid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for NavigationGrid.
 */
class NavigationGridTest {
    private static final int GRID_WIDTH = 6;
    private static final int GRID_HEIGHT = 6;
    private static final float CELL_SIZE = 32.0f;
    private static final float ORIGIN = 0.0f;
    private static final float HALF_CELL = 16.0f;
    private static final float AGENT_RADIUS_ONE_CELL = 32.0f;

    /**
     * Verifies that world and grid coordinates round trip through cell centers.
     */
    @Test
    void worldAndGridCoordinatesRoundTripThroughCellCenters() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        Assertions.assertEquals(16.0f, grid.gridToWorldX(0));
        Assertions.assertEquals(16.0f, grid.gridToWorldY(0));
        Assertions.assertEquals(2, grid.worldToGridX(80.0f));
        Assertions.assertEquals(3, grid.worldToGridY(96.0f));
    }

    /**
     * Verifies that temporarily blocked area changes walkability until unblocked.
     */
    @Test
    void temporarilyBlockedAreaChangesWalkabilityUntilUnblocked() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        grid.temporarilyBlockArea(2, 2, 1);
        Assertions.assertFalse(grid.isWalkable(1, 1));
        Assertions.assertFalse(grid.isWalkable(2, 2));
        Assertions.assertFalse(grid.isWalkable(3, 3));
        grid.temporarilyUnblockArea(2, 2, 1);
        Assertions.assertTrue(grid.isWalkable(1, 1));
        Assertions.assertTrue(grid.isWalkable(2, 2));
        Assertions.assertTrue(grid.isWalkable(3, 3));
    }

    /**
     * Verifies that walkability with radius fails when nearby cell is blocked.
     */
    @Test
    void walkabilityWithRadiusFailsWhenNearbyCellIsBlocked() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        grid.temporarilyBlockCell(3, 2);
        Assertions.assertFalse(grid.isWalkableWithRadius(2, 2, 32.0f));
        Assertions.assertTrue(grid.isWalkableWithRadius(0, 0, 0.0f));
    }

    /**
     * Verifies that build base from tiled marks missing cells as blocked.
     */
    @Test
    void buildBaseFromTiledMarksMissingCellsAsBlocked() {
        NavigationGrid grid = new NavigationGrid(6, 6, 32.0f, 0.0f, 0.0f);
        TiledMapTileLayer layer = new TiledMapTileLayer(6, 6, 32, 32);
        layer.setCell(0, 0, new TiledMapTileLayer.Cell());
        layer.setCell(1, 1, new TiledMapTileLayer.Cell());
        grid.buildBaseFromTiled(layer);
        Assertions.assertTrue(grid.isWalkableStaticOnly(0, 0));
        Assertions.assertTrue(grid.isWalkableStaticOnly(1, 1));
        Assertions.assertFalse(grid.isWalkableStaticOnly(2, 2));
    }
}