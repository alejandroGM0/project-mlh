package io.github.proyectoM.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.pathfinding.NavigationGrid;
import io.github.proyectoM.resources.Assets;
import java.util.Locale;
import java.util.Objects;

/** Loads, renders, and queries the currently active Tiled map. */
public class MapManager {
  private static final String DEFAULT_MAP_PATH = "maps/newIsometricMap.tmx";
  private static final String SPAWN_LAYER_NAME = "spawn";
  private static final String MAP_WIDTH_KEY = "width";
  private static final String MAP_HEIGHT_KEY = "height";
  private static final String TILE_WIDTH_KEY = "tilewidth";
  private static final String TILE_HEIGHT_KEY = "tileheight";
  private static final String BASE_LAYER_NAME = "base";
  private static final String LEGACY_BASE_LAYER_NAME = "background";
  private static final float GRID_ORIGIN = 0f;
  private static final String NO_MAP_INFO = "No map loaded";
  private static final String MAP_INFO_TEMPLATE = "Map: %dx%d tiles (%dx%d px), %d layers";

  private TiledMap currentMap;
  private IsometricTiledMapRenderer mapRenderer;
  private final MapCollisionLoader collisionSystem;
  private boolean mapLoaded;

  public MapManager(World world) {
    this.collisionSystem = new MapCollisionLoader(world);
  }

  public void loadMap(String mapPath) {
    Objects.requireNonNull(mapPath, "mapPath");
    unloadMapIfAlreadyLoaded(mapPath);
    disposeRenderer();

    Assets.getManager().load(mapPath, TiledMap.class);
    Assets.getManager().finishLoadingAsset(mapPath);

    currentMap = Assets.getManager().get(mapPath, TiledMap.class);
    mapRenderer = new IsometricTiledMapRenderer(currentMap);
    collisionSystem.createMapCollisions(currentMap);
    mapLoaded = true;
  }

  public void loadDefaultMap() {
    loadMap(DEFAULT_MAP_PATH);
  }

  public void render(OrthographicCamera camera) {
    if (!isMapLoaded()) {
      return;
    }

    mapRenderer.setView(Objects.requireNonNull(camera, "camera"));
    mapRenderer.render();
  }

  public boolean isMapLoaded() {
    return mapLoaded && currentMap != null;
  }

  public TiledMap getCurrentMap() {
    return currentMap;
  }

  public NavigationGrid buildNavigationGrid() {
    if (!isMapLoaded()) {
      return null;
    }

    int mapWidth = getRequiredIntProperty(MAP_WIDTH_KEY);
    int mapHeight = getRequiredIntProperty(MAP_HEIGHT_KEY);
    int tileWidth = getRequiredIntProperty(TILE_WIDTH_KEY);

    NavigationGrid navigationGrid =
        new NavigationGrid(mapWidth, mapHeight, tileWidth, GRID_ORIGIN, GRID_ORIGIN);

    TiledMapTileLayer baseLayer = findBaseLayer();
    if (baseLayer != null) {
      navigationGrid.buildBaseFromTiled(baseLayer);
    }

    navigationGrid.buildCollisionsFromSystem(collisionSystem);
    return navigationGrid;
  }

  /**
   * Returns the spawn point defined in the map's "spawn" object layer as world pixel coordinates,
   * or {@code null} if no spawn layer or object is present.
   *
   * @return world-space spawn position in pixels, or null
   */
  public Vector2 getSpawnPoint() {
    if (!isMapLoaded()) {
      return null;
    }

    MapLayer spawnLayer = currentMap.getLayers().get(SPAWN_LAYER_NAME);
    if (spawnLayer == null || spawnLayer.getObjects().getCount() == 0) {
      return null;
    }

    MapObject spawnObject = spawnLayer.getObjects().get(0);
    float rawX = spawnObject.getProperties().get("x", Float.class);
    float rawY = spawnObject.getProperties().get("y", Float.class);
    return TiledCoordinates.toIsometric(rawX, rawY, currentMap);
  }

  public String getMapInfo() {
    if (!isMapLoaded()) {
      return NO_MAP_INFO;
    }

    int mapWidth = getRequiredIntProperty(MAP_WIDTH_KEY);
    int mapHeight = getRequiredIntProperty(MAP_HEIGHT_KEY);
    int tileWidth = getRequiredIntProperty(TILE_WIDTH_KEY);
    int tileHeight = getRequiredIntProperty(TILE_HEIGHT_KEY);
    int layerCount = currentMap.getLayers().getCount();

    return String.format(
        Locale.ROOT, MAP_INFO_TEMPLATE, mapWidth, mapHeight, tileWidth, tileHeight, layerCount);
  }

  public void dispose() {
    disposeRenderer();
    collisionSystem.dispose();
    currentMap = null;
    mapLoaded = false;
  }

  private void unloadMapIfAlreadyLoaded(String mapPath) {
    if (Assets.getManager().isLoaded(mapPath)) {
      Assets.getManager().unload(mapPath);
    }
  }

  private void disposeRenderer() {
    if (mapRenderer != null) {
      mapRenderer.dispose();
      mapRenderer = null;
    }
  }

  private int getRequiredIntProperty(String key) {
    Integer value = currentMap.getProperties().get(key, Integer.class);
    if (value == null) {
      throw new IllegalStateException("Missing map property: " + key);
    }
    return value;
  }

  private TiledMapTileLayer findBaseLayer() {
    TiledMapTileLayer baseLayer = getTileLayer(BASE_LAYER_NAME);
    if (baseLayer != null) {
      return baseLayer;
    }

    return getTileLayer(LEGACY_BASE_LAYER_NAME);
  }

  private TiledMapTileLayer getTileLayer(String layerName) {
    MapLayer layer = currentMap.getLayers().get(layerName);
    if (layer instanceof TiledMapTileLayer) {
      return (TiledMapTileLayer) layer;
    }
    return null;
  }
}
