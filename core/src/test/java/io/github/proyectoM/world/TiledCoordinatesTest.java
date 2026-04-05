package io.github.proyectoM.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.world.TiledCoordinates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for TiledCoordinates conversions.
 */
class TiledCoordinatesTest {
    private static final float EPSILON = 1.0E-4f;
    private static final String ORIENTATION_KEY = "orientation";
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    private static final String TILE_WIDTH_KEY = "tilewidth";
    private static final String TILE_HEIGHT_KEY = "tileheight";
    private static final String ISOMETRIC_ORIENTATION = "isometric";

    /**
     * Verifies that get flipped tiled center uses orthogonal coordinates when map is not isometric.
     */
    @Test
    void getFlippedTiledCenterUsesOrthogonalCoordinatesWhenMapIsNotIsometric() {
        TiledMap map = TiledCoordinatesTest.createMap(null, 10, 5, 64, 32);
        Vector2 center = TiledCoordinates.getFlippedTiledCenter((Rectangle)new Rectangle(10.0f, 20.0f, 8.0f, 4.0f), (TiledMap)map);
        Assertions.assertEquals(14.0f, center.x, 1.0E-4f);
        Assertions.assertEquals(218.0f, center.y, 1.0E-4f);
    }

    /**
     * Verifies that get flipped tiled center uses isometric coordinates when orientation matches.
     */
    @Test
    void getFlippedTiledCenterUsesIsometricCoordinatesWhenOrientationMatches() {
        TiledMap map = TiledCoordinatesTest.createMap(ISOMETRIC_ORIENTATION, 10, 10, 64, 32);
        Vector2 center = TiledCoordinates.getFlippedTiledCenter((Rectangle)new Rectangle(32.0f, 32.0f, 32.0f, 32.0f), (TiledMap)map);
        Assertions.assertEquals(96.0f, center.x, 1.0E-4f);
        Assertions.assertEquals(0.0f, center.y, 1.0E-4f);
    }

    /**
     * Verifies that to isometric falls back to original coordinates when tile size is missing.
     */
    @Test
    void toIsometricFallsBackToOriginalCoordinatesWhenTileSizeIsMissing() {
        TiledMap map = new TiledMap();
        Vector2 isometric = TiledCoordinates.toIsometric(12.0f, 34.0f, (TiledMap)map);
        Assertions.assertEquals(12.0f, isometric.x, 1.0E-4f);
        Assertions.assertEquals(34.0f, isometric.y, 1.0E-4f);
    }

    private static TiledMap createMap(String orientation, int width, int height, int tileWidth, int tileHeight) {
        TiledMap map = new TiledMap();
        if (orientation != null) {
            map.getProperties().put(ORIENTATION_KEY, orientation);
        }
        map.getProperties().put(WIDTH_KEY, width);
        map.getProperties().put(HEIGHT_KEY, height);
        map.getProperties().put(TILE_WIDTH_KEY, tileWidth);
        map.getProperties().put(TILE_HEIGHT_KEY, tileHeight);
        return map;
    }
}