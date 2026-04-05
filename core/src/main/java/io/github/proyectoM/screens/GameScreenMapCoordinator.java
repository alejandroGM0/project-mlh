package io.github.proyectoM.screens;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.pathfinding.NavigationGrid;
import io.github.proyectoM.registry.MapRegistry;
import io.github.proyectoM.templates.MapTemplate;
import io.github.proyectoM.world.LightMapLoader;
import io.github.proyectoM.world.MapManager;

/** Manages the gameplay map, navigation grid, and light entities for {@link GameScreen}. */
final class GameScreenMapCoordinator {
  private final World world;
  private final PooledEngine engine;
  private final RayHandler rayHandler;
  private String mapId;

  private MapManager mapManager;
  private NavigationGrid navigationGrid;

  GameScreenMapCoordinator(World world, PooledEngine engine, RayHandler rayHandler) {
    this.world = world;
    this.engine = engine;
    this.rayHandler = rayHandler;
  }

  /**
   * Initializes the map using the default map path.
   */
  void initialize() {
    initialize(null);
  }

  /**
   * Initializes the map using the given registry map identifier.
   *
   * @param mapId the registry map id to load, or null for the default map
   */
  void initialize(String mapId) {
    this.mapId = mapId;
    mapManager = new MapManager(world);
    loadMapById(mapId);
    rebuildMapState();
  }

  void reload() {
    clearLightEntities();
    disposeMapManager();
    initialize(mapId);
  }

  /**
   * Loads a map by registry id, falling back to the default map when id is null or not found.
   *
   * @param mapId the registry map identifier, or null
   */
  private void loadMapById(String mapId) {
    if (mapId != null) {
      MapTemplate template = MapRegistry.getInstance().getTemplate(mapId);
      if (template != null) {
        mapManager.loadMap(template.tmxPath);
        return;
      }
    }
    mapManager.loadDefaultMap();
  }

  void render(OrthographicCamera camera) {
    if (mapManager != null && mapManager.isMapLoaded()) {
      mapManager.render(camera);
    }
  }

  NavigationGrid getNavigationGrid() {
    return navigationGrid;
  }

  /**
   * Returns the spawn point defined in the loaded map's spawn layer as world pixel coordinates, or
   * {@code null} if no spawn point is present.
   *
   * @return world-space spawn position in pixels, or null
   */
  Vector2 getSpawnPoint() {
    if (mapManager == null) {
      return null;
    }
    return mapManager.getSpawnPoint();
  }

  void dispose() {
    disposeMapManager();
  }

  private void rebuildMapState() {
    TiledMap currentMap = mapManager.getCurrentMap();
    navigationGrid = mapManager.buildNavigationGrid();
    LightMapLoader.createLights(currentMap, engine);
    applyAmbientFromMap(currentMap);
  }

  private void applyAmbientFromMap(TiledMap map) {
    if (map == null || rayHandler == null) {
      return;
    }

    float ambientIntensity = LightMapLoader.readAmbientIntensity(map);
    Color ambientColor = LightMapLoader.readAmbientColor(map);
    rayHandler.setAmbientLight(
        ambientColor.r * ambientIntensity,
        ambientColor.g * ambientIntensity,
        ambientColor.b * ambientIntensity,
        ambientIntensity);
  }

  private void clearLightEntities() {
    for (Entity entity : engine.getEntitiesFor(Family.all(LightComponent.class).get())) {
      engine.removeEntity(entity);
    }
  }

  private void disposeMapManager() {
    if (mapManager != null) {
      mapManager.dispose();
      mapManager = null;
      navigationGrid = null;
    }
  }
}
