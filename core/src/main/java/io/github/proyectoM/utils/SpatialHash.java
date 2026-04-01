package io.github.proyectoM.utils;

import com.badlogic.ashley.core.Entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A spatial hash grid for efficient searching of nearby entities. */
public final class SpatialHash {
  private static final int CELL_KEY_SHIFT_BITS = 32;
  private static final long CELL_KEY_MASK = 0xFFFFFFFFL;

  private final float cellSize;
  private final Map<Long, List<Entity>> grid;

  public SpatialHash(float cellSize) {
    if (cellSize <= 0f) {
      throw new IllegalArgumentException("SpatialHash cell size must be positive.");
    }
    this.cellSize = cellSize;
    this.grid = new HashMap<>();
  }

  public void clear() {
    grid.clear();
  }

  public void insert(Entity entity, float x, float y) {
    long key = getKey(x, y);
    grid.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entity);
  }

  public void queryRadius(float x, float y, float radius, List<Entity> result) {
    result.clear();

    int cellRadius = (int) Math.ceil(radius / cellSize);
    int centerCellX = getCellX(x);
    int centerCellY = getCellY(y);

    for (int dx = -cellRadius; dx <= cellRadius; dx++) {
      for (int dy = -cellRadius; dy <= cellRadius; dy++) {
        int cellX = centerCellX + dx;
        int cellY = centerCellY + dy;
        long key = getKey(cellX, cellY);

        List<Entity> cellEntities = grid.get(key);
        if (cellEntities != null) {
          result.addAll(cellEntities);
        }
      }
    }
  }

  public float getCellSize() {
    return cellSize;
  }

  public int getOccupiedCellCount() {
    return grid.size();
  }

  private int getCellX(float x) {
    return (int) Math.floor(x / cellSize);
  }

  private int getCellY(float y) {
    return (int) Math.floor(y / cellSize);
  }

  private long getKey(float x, float y) {
    return getKey(getCellX(x), getCellY(y));
  }

  private long getKey(int cellX, int cellY) {
    return ((long) cellX << CELL_KEY_SHIFT_BITS) | (cellY & CELL_KEY_MASK);
  }
}
