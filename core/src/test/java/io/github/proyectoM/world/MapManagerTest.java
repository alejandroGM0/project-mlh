package io.github.proyectoM.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import io.github.proyectoM.pathfinding.NavigationGrid;
import io.github.proyectoM.world.MapManager;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for MapManager.
 */
class MapManagerTest {
    private static final String CURRENT_MAP_FIELD = "currentMap";
    private static final String MAP_LOADED_FIELD = "mapLoaded";
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    private static final String TILE_WIDTH_KEY = "tilewidth";
    private static final String TILE_HEIGHT_KEY = "tileheight";
    private static final String BASE_LAYER_NAME = "base";
    private static final String LEGACY_BASE_LAYER_NAME = "background";

    /**
     * Verifies that build navigation grid returns null when no map is loaded.
     */
    @Test
    void buildNavigationGridReturnsNullWhenNoMapIsLoaded() {
        MapManager mapManager = new MapManager(null);
        Assertions.assertNull(mapManager.buildNavigationGrid());
        Assertions.assertEquals("No map loaded", mapManager.getMapInfo());
    }

    /**
     * Verifies that build navigation grid uses base layer before legacy background layer.
     */
    @Test
    void buildNavigationGridUsesBaseLayerBeforeLegacyBackgroundLayer() throws ReflectiveOperationException {
        MapManager mapManager = new MapManager(null);
        TiledMap map = MapManagerTest.createMapWithDimensions();
        TiledMapTileLayer baseLayer = MapManagerTest.createLayer(BASE_LAYER_NAME, true, false);
        TiledMapTileLayer backgroundLayer = MapManagerTest.createLayer(LEGACY_BASE_LAYER_NAME, false, true);
        map.getLayers().add((MapLayer)baseLayer);
        map.getLayers().add((MapLayer)backgroundLayer);
        MapManagerTest.setLoadedMap(mapManager, map);
        NavigationGrid navigationGrid = mapManager.buildNavigationGrid();
        Assertions.assertNotNull(navigationGrid);
        Assertions.assertEquals(2, navigationGrid.getGridWidth());
        Assertions.assertEquals(2, navigationGrid.getGridHeight());
        Assertions.assertTrue(navigationGrid.isWalkable(0, 0));
        Assertions.assertFalse(navigationGrid.isWalkable(1, 1));
    }

    /**
     * Verifies that build navigation grid falls back to legacy background layer.
     */
    @Test
    void buildNavigationGridFallsBackToLegacyBackgroundLayer() throws ReflectiveOperationException {
        MapManager mapManager = new MapManager(null);
        TiledMap map = MapManagerTest.createMapWithDimensions();
        TiledMapTileLayer backgroundLayer = MapManagerTest.createLayer(LEGACY_BASE_LAYER_NAME, false, true);
        map.getLayers().add((MapLayer)backgroundLayer);
        MapManagerTest.setLoadedMap(mapManager, map);
        NavigationGrid navigationGrid = mapManager.buildNavigationGrid();
        Assertions.assertNotNull(navigationGrid);
        Assertions.assertFalse(navigationGrid.isWalkable(0, 0));
        Assertions.assertTrue(navigationGrid.isWalkable(1, 1));
    }

    /**
     * Verifies that get map info summarizes loaded map.
     */
    @Test
    void getMapInfoSummarizesLoadedMap() throws ReflectiveOperationException {
        MapManager mapManager = new MapManager(null);
        TiledMap map = MapManagerTest.createMapWithDimensions();
        map.getLayers().add((MapLayer)MapManagerTest.createLayer(BASE_LAYER_NAME, true, false));
        MapManagerTest.setLoadedMap(mapManager, map);
        Assertions.assertEquals("Map: 2x2 tiles (64x32 px), 1 layers", mapManager.getMapInfo());
    }

    private static TiledMap createMapWithDimensions() {
        TiledMap map = new TiledMap();
        map.getProperties().put(WIDTH_KEY, 2);
        map.getProperties().put(HEIGHT_KEY, 2);
        map.getProperties().put(TILE_WIDTH_KEY, 64);
        map.getProperties().put(TILE_HEIGHT_KEY, 32);
        return map;
    }

    private static TiledMapTileLayer createLayer(String layerName, boolean topLeftWalkable, boolean bottomRightWalkable) {
        TiledMapTileLayer layer = new TiledMapTileLayer(2, 2, 64, 32);
        layer.setName(layerName);
        if (topLeftWalkable) {
            layer.setCell(0, 0, new TiledMapTileLayer.Cell());
        }
        if (bottomRightWalkable) {
            layer.setCell(1, 1, new TiledMapTileLayer.Cell());
        }
        return layer;
    }

    private static void setLoadedMap(MapManager mapManager, TiledMap map) throws ReflectiveOperationException {
        Field currentMapField = MapManager.class.getDeclaredField(CURRENT_MAP_FIELD);
        currentMapField.setAccessible(true);
        currentMapField.set(mapManager, map);
        Field mapLoadedField = MapManager.class.getDeclaredField(MAP_LOADED_FIELD);
        mapLoadedField.setAccessible(true);
        mapLoadedField.setBoolean(mapManager, true);
    }
}