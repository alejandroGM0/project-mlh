package io.github.proyectoM.templates;

/** Mutable data template populated from `data/maps.json`. */
public class MapTemplate {
  public static final float DEFAULT_SPAWN_POSITION = 0f;
  public static final int DEFAULT_MAP_DIMENSION = 0;
  public static final int DEFAULT_TILE_SIZE = 32;

  public String id;
  public String tmxPath;
  public String name;
  public float spawnX = DEFAULT_SPAWN_POSITION;
  public float spawnY = DEFAULT_SPAWN_POSITION;
  public String backgroundMusic;
  public String lightingConfig;
  public int mapWidth = DEFAULT_MAP_DIMENSION;
  public int mapHeight = DEFAULT_MAP_DIMENSION;
  public int tileSize = DEFAULT_TILE_SIZE;
}
