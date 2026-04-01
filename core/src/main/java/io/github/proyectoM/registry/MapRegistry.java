package io.github.proyectoM.registry;

import com.badlogic.gdx.utils.JsonValue;
import io.github.proyectoM.templates.MapTemplate;

/** Loads and exposes map templates defined in {@code data/maps.json}. */
public final class MapRegistry extends AbstractJsonRegistry<MapTemplate> {
  private static final String MAPS_JSON_PATH = "data/maps.json";
  private static final int DEFAULT_MAP_DIMENSION = 0;
  private static final int DEFAULT_TILE_SIZE = 32;
  private static final float DEFAULT_SPAWN_POSITION = 0f;

  private static final MapRegistry INSTANCE = new MapRegistry();

  private MapRegistry() {}

  public static MapRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getJsonPath() {
    return MAPS_JSON_PATH;
  }

  @Override
  protected String getId(MapTemplate template) {
    return template.id;
  }

  @Override
  protected MapTemplate readTemplate(JsonValue mapNode) {
    MapTemplate template = new MapTemplate();
    template.id = mapNode.getString("id");
    template.tmxPath = mapNode.getString("tmxPath");
    template.name = mapNode.getString("name", template.id);
    template.spawnX = mapNode.getFloat("spawnX", DEFAULT_SPAWN_POSITION);
    template.spawnY = mapNode.getFloat("spawnY", DEFAULT_SPAWN_POSITION);
    template.backgroundMusic = mapNode.getString("backgroundMusic", null);
    template.lightingConfig = mapNode.getString("lightingConfig", null);
    template.mapWidth = mapNode.getInt("mapWidth", DEFAULT_MAP_DIMENSION);
    template.mapHeight = mapNode.getInt("mapHeight", DEFAULT_MAP_DIMENSION);
    template.tileSize = mapNode.getInt("tileSize", DEFAULT_TILE_SIZE);
    return template;
  }
}
