package io.github.proyectoM.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Converts Tiled coordinates into the world-space coordinates used by the game. */
public final class TiledCoordinates {
  private static final String ORIENTATION_KEY = "orientation";
  private static final String ISOMETRIC_ORIENTATION = "isometric";
  private static final String TILE_WIDTH_KEY = "tilewidth";
  private static final String TILE_HEIGHT_KEY = "tileheight";
  private static final String MAP_WIDTH_KEY = "width";
  private static final String MAP_HEIGHT_KEY = "height";
  private static final float HALF_TILE = 0.5f;

  private TiledCoordinates() {}

  public static Vector2 getFlippedTiledCenter(Rectangle tiledRect, TiledMap map) {
    float tiledCenterX = tiledRect.x + tiledRect.width * HALF_TILE;
    float tiledCenterY = tiledRect.y + tiledRect.height * HALF_TILE;

    String orientation =
        map != null ? map.getProperties().get(ORIENTATION_KEY, String.class) : null;
    if (ISOMETRIC_ORIENTATION.equalsIgnoreCase(orientation)) {
      return convertTiledToIsometric(tiledCenterX, tiledCenterY, map);
    }

    return convertTiledToOrthogonal(tiledCenterX, tiledCenterY, map);
  }

  public static Vector2 toIsometric(float tiledX, float tiledY, TiledMap map) {
    return convertTiledToIsometric(tiledX, tiledY, map);
  }

  private static Vector2 convertTiledToIsometric(float tiledX, float tiledY, TiledMap map) {
    Integer tileWidth = map.getProperties().get(TILE_WIDTH_KEY, Integer.class);
    Integer tileHeight = map.getProperties().get(TILE_HEIGHT_KEY, Integer.class);
    if (tileWidth == null || tileHeight == null) {
      return new Vector2(tiledX, tiledY);
    }

    float halfTileWidth = tileWidth * HALF_TILE;
    float halfTileHeight = tileHeight * HALF_TILE;
    float col = tiledX / tileHeight;
    float row = tiledY / tileHeight;
    float x = (col * halfTileWidth) + (row * halfTileWidth);
    float y = (row * halfTileHeight) - (col * halfTileHeight);
    return new Vector2(x, y);
  }

  private static Vector2 convertTiledToOrthogonal(float tiledX, float tiledY, TiledMap map) {
    float mapHeight = getMapPixelHeight(map);
    if (mapHeight == 0f) {
      return new Vector2(tiledX, tiledY);
    }

    return new Vector2(tiledX, mapHeight - tiledY);
  }

  private static float getMapPixelHeight(TiledMap map) {
    if (map == null) {
      return 0f;
    }

    Integer mapWidthTiles = map.getProperties().get(MAP_WIDTH_KEY, Integer.class);
    Integer mapHeightTiles = map.getProperties().get(MAP_HEIGHT_KEY, Integer.class);
    Integer tileHeight = map.getProperties().get(TILE_HEIGHT_KEY, Integer.class);
    if (mapWidthTiles == null || mapHeightTiles == null || tileHeight == null) {
      return 0f;
    }

    return (mapWidthTiles + mapHeightTiles) * tileHeight * HALF_TILE;
  }
}
