package io.github.proyectoM.pathfinding;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.world.MapCollisionLoader;
import java.util.HashSet;
import java.util.Set;

/** Stores static and dynamic pathfinding walkability for a grid-based map. */
public class NavigationGrid {
  private static final boolean WALKABLE = true;
  private static final boolean BLOCKED = false;
  private static final boolean RECALCULATION_NOT_NEEDED = false;
  private static final boolean RECALCULATION_NEEDED = true;

  private static final float MIN_VALID_OBSTACLE_SIZE_METERS = 0.1f;
  private static final float MIN_VALID_OBSTACLE_POSITION_METERS = 0.1f;
  private static final int SHAPE_DIAMETER_MULTIPLIER = 2;
  private static final int MIN_OBSTACLE_CELL_RADIUS = 1;
  private static final int PIXEL_RECTANGLE_END_OFFSET = 1;
  private static final float CELL_CENTER_OFFSET = 0.5f;

  private final int gridWidth;
  private final int gridHeight;
  private final float cellSize;
  private final float originX;
  private final float originY;

  private final boolean[][] baseGrid;
  private final boolean[][] dynamicGrid;
  private final boolean[][] finalGrid;

  private boolean needsRecalculation = RECALCULATION_NOT_NEEDED;
  private final Set<Vector2> dynamicObstacles = new HashSet<>();

  /**
   * Creates a navigation grid for the given dimensions and world origin.
   *
   * @param gridWidth grid width in cells
   * @param gridHeight grid height in cells
   * @param cellSize cell size in world units
   * @param originX world-space x origin
   * @param originY world-space y origin
   */
  public NavigationGrid(
      int gridWidth, int gridHeight, float cellSize, float originX, float originY) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.cellSize = cellSize;
    this.originX = originX;
    this.originY = originY;

    this.baseGrid = new boolean[gridWidth][gridHeight];
    this.dynamicGrid = new boolean[gridWidth][gridHeight];
    this.finalGrid = new boolean[gridWidth][gridHeight];

    initializeGrids();
  }

  private void initializeGrids() {
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        baseGrid[x][y] = WALKABLE;
        dynamicGrid[x][y] = WALKABLE;
        finalGrid[x][y] = WALKABLE;
      }
    }
  }

  /**
   * Builds the static walkability grid from a tiled layer.
   *
   * @param baseLayer floor or base map layer
   */
  public void buildBaseFromTiled(TiledMapTileLayer baseLayer) {
    if (baseLayer == null) {
      return;
    }

    int layerWidth = baseLayer.getWidth();
    int layerHeight = baseLayer.getHeight();

    for (int x = 0; x < gridWidth && x < layerWidth; x++) {
      for (int y = 0; y < gridHeight && y < layerHeight; y++) {
        TiledMapTileLayer.Cell cell = baseLayer.getCell(x, y);
        baseGrid[x][y] = cell != null;
      }
    }

    markForRecalculation();
  }

  /**
   * Updates dynamic obstacles from the current Box2D world.
   *
   * @param world physics world containing obstacles
   */
  public void updateDynamicObstacles(World world) {
    resetDynamicGridCells();

    Array<Body> bodies = new Array<>();
    world.getBodies(bodies);

    for (Body body : bodies) {
      if (body.getType() == BodyDef.BodyType.StaticBody && isValidMapObstacle(body)) {
        markBodyAsObstacle(body);
      }
    }

    markForRecalculation();
  }

  private void resetDynamicGridCells() {
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        dynamicGrid[x][y] = WALKABLE;
      }
    }
  }

  private boolean isValidMapObstacle(Body body) {
    Vector2 bodyPos = body.getPosition();

    for (Fixture fixture : body.getFixtureList()) {
      float size = getFixtureSizeInMeters(fixture);
      if (size < MIN_VALID_OBSTACLE_SIZE_METERS) {
        return false;
      }

      if (Math.abs(bodyPos.x) < MIN_VALID_OBSTACLE_POSITION_METERS
          && Math.abs(bodyPos.y) < MIN_VALID_OBSTACLE_POSITION_METERS) {
        return false;
      }
    }

    return true;
  }

  private float getFixtureSizeInMeters(Fixture fixture) {
    Shape shape = fixture.getShape();
    if (shape instanceof PolygonShape) {
      PolygonShape polygonShape = (PolygonShape) shape;
      Vector2 vertex = new Vector2();
      polygonShape.getVertex(0, vertex);
      return Math.max(Math.abs(vertex.x), Math.abs(vertex.y)) * SHAPE_DIAMETER_MULTIPLIER;
    }

    if (shape instanceof CircleShape) {
      CircleShape circleShape = (CircleShape) shape;
      return circleShape.getRadius() * SHAPE_DIAMETER_MULTIPLIER;
    }

    return MIN_VALID_OBSTACLE_SIZE_METERS;
  }

  private void markBodyAsObstacle(Body body) {
    Vector2 bodyPos = body.getPosition();
    float bodyPixelX = bodyPos.x * PhysicsConstants.PIXELS_PER_METER;
    float bodyPixelY = bodyPos.y * PhysicsConstants.PIXELS_PER_METER;

    for (Fixture fixture : body.getFixtureList()) {
      float fixtureSize = getFixtureSizeInPixels(fixture);
      int gridX = worldToGridX(bodyPixelX);
      int gridY = worldToGridY(bodyPixelY);
      int cellRadius = Math.max(MIN_OBSTACLE_CELL_RADIUS, (int) (fixtureSize / cellSize));

      markCellArea(gridX, gridY, cellRadius, BLOCKED);
    }
  }

  private float getFixtureSizeInPixels(Fixture fixture) {
    Shape shape = fixture.getShape();
    if (shape instanceof PolygonShape) {
      PolygonShape polygonShape = (PolygonShape) shape;
      Vector2 vertex = new Vector2();
      polygonShape.getVertex(0, vertex);
      float sizeMeters =
          Math.max(Math.abs(vertex.x), Math.abs(vertex.y)) * SHAPE_DIAMETER_MULTIPLIER;
      return sizeMeters * PhysicsConstants.PIXELS_PER_METER;
    }

    if (shape instanceof CircleShape) {
      CircleShape circleShape = (CircleShape) shape;
      float diameterMeters = circleShape.getRadius() * SHAPE_DIAMETER_MULTIPLIER;
      return diameterMeters * PhysicsConstants.PIXELS_PER_METER;
    }

    return cellSize;
  }

  /**
   * Adds a temporary dynamic obstacle in world space.
   *
   * @param worldX obstacle x position
   * @param worldY obstacle y position
   * @param radius obstacle radius in world units
   */
  public void addTemporaryObstacle(float worldX, float worldY, float radius) {
    dynamicObstacles.add(new Vector2(worldX, worldY));

    int gridX = worldToGridX(worldX);
    int gridY = worldToGridY(worldY);
    int cellRadius = Math.max(MIN_OBSTACLE_CELL_RADIUS, (int) (radius / cellSize));
    markCellArea(gridX, gridY, cellRadius, BLOCKED);
    markForRecalculation();
  }

  /**
   * Removes a temporary dynamic obstacle.
   *
   * @param worldX obstacle x position
   * @param worldY obstacle y position
   */
  public void removeTemporaryObstacle(float worldX, float worldY) {
    if (dynamicObstacles.remove(new Vector2(worldX, worldY))) {
      markForRecalculation();
    }
  }

  /** Clears all temporary dynamic obstacles. */
  public void clearDynamicObstacles() {
    dynamicObstacles.clear();
    resetDynamicGrid();
  }

  /** Rebuilds the final walkability grid from static and dynamic state. */
  public void recalculateFinalGrid() {
    if (!needsRecalculation) {
      return;
    }

    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        finalGrid[x][y] = baseGrid[x][y] && dynamicGrid[x][y];
      }
    }

    needsRecalculation = RECALCULATION_NOT_NEEDED;
  }

  /**
   * Returns whether a grid cell is walkable.
   *
   * @param gridX grid x coordinate
   * @param gridY grid y coordinate
   * @return true when the cell is walkable
   */
  public boolean isWalkable(int gridX, int gridY) {
    if (!isValidGridPosition(gridX, gridY)) {
      return false;
    }

    recalculateFinalGrid();
    return finalGrid[gridX][gridY];
  }

  /**
   * Returns whether a grid cell is walkable for an agent radius.
   *
   * @param gridX grid x coordinate
   * @param gridY grid y coordinate
   * @param agentRadiusPixels agent radius in pixels
   * @return true when the area is walkable
   */
  public boolean isWalkableWithRadius(int gridX, int gridY, float agentRadiusPixels) {
    if (!isValidGridPosition(gridX, gridY)) {
      return false;
    }

    recalculateFinalGrid();
    int cellRadius = Math.max(0, (int) (agentRadiusPixels / cellSize));
    return isAreaWalkable(finalGrid, gridX, gridY, cellRadius);
  }

  /**
   * Returns whether a grid cell is walkable against static obstacles only.
   *
   * @param gridX grid x coordinate
   * @param gridY grid y coordinate
   * @return true when the static map allows movement
   */
  public boolean isWalkableStaticOnly(int gridX, int gridY) {
    if (!isValidGridPosition(gridX, gridY)) {
      return false;
    }

    return baseGrid[gridX][gridY];
  }

  /**
   * Returns whether a grid cell is walkable for an agent radius using only static obstacles.
   *
   * @param gridX grid x coordinate
   * @param gridY grid y coordinate
   * @param agentRadiusPixels agent radius in pixels
   * @return true when the static area is walkable
   */
  public boolean isWalkableWithRadiusStaticOnly(int gridX, int gridY, float agentRadiusPixels) {
    if (!isValidGridPosition(gridX, gridY)) {
      return false;
    }

    int cellRadius = Math.max(0, (int) (agentRadiusPixels / cellSize));
    return isAreaWalkable(baseGrid, gridX, gridY, cellRadius);
  }

  private boolean isAreaWalkable(boolean[][] grid, int gridX, int gridY, int cellRadius) {
    for (int dx = -cellRadius; dx <= cellRadius; dx++) {
      for (int dy = -cellRadius; dy <= cellRadius; dy++) {
        int checkX = gridX + dx;
        int checkY = gridY + dy;
        if (!isValidGridPosition(checkX, checkY) || !grid[checkX][checkY]) {
          return false;
        }
      }
    }

    return true;
  }

  public int worldToGridX(float worldX) {
    return (int) ((worldX - originX) / cellSize);
  }

  public int worldToGridY(float worldY) {
    return (int) ((worldY - originY) / cellSize);
  }

  public float gridToWorldX(int gridX) {
    return originX + (gridX + CELL_CENTER_OFFSET) * cellSize;
  }

  public float gridToWorldY(int gridY) {
    return originY + (gridY + CELL_CENTER_OFFSET) * cellSize;
  }

  public boolean isValidGridPosition(int gridX, int gridY) {
    return gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight;
  }

  private void markForRecalculation() {
    needsRecalculation = RECALCULATION_NEEDED;
  }

  public void markCellAsBlocked(int gridX, int gridY) {
    if (isValidGridPosition(gridX, gridY)) {
      dynamicGrid[gridX][gridY] = BLOCKED;
      markForRecalculation();
    }
  }

  public void resetDynamicGrid() {
    resetDynamicGridCells();
    markForRecalculation();
  }

  /**
   * Builds static collisions from the already processed map collision loader.
   *
   * @param collisionSystem collision loader containing blocking rectangles
   */
  public void buildCollisionsFromSystem(MapCollisionLoader collisionSystem) {
    if (collisionSystem == null) {
      return;
    }

    Array<Rectangle> collisionRectangles = collisionSystem.getCollisionRectangles();
    for (Rectangle rect : collisionRectangles) {
      markRectangleAsBlockedInBaseGrid(rect.x, rect.y, rect.width, rect.height);
    }

    markForRecalculation();
  }

  private void markRectangleAsBlockedInBaseGrid(float x, float y, float width, float height) {
    int startGridX = worldToGridX(x);
    int startGridY = worldToGridY(y);
    int endGridX = worldToGridX(x + width - PIXEL_RECTANGLE_END_OFFSET);
    int endGridY = worldToGridY(y + height - PIXEL_RECTANGLE_END_OFFSET);

    startGridX = clampGridX(startGridX);
    startGridY = clampGridY(startGridY);
    endGridX = clampGridX(endGridX);
    endGridY = clampGridY(endGridY);

    for (int gx = startGridX; gx <= endGridX; gx++) {
      for (int gy = startGridY; gy <= endGridY; gy++) {
        baseGrid[gx][gy] = BLOCKED;
      }
    }
  }

  public void temporarilyUnblockCell(int gridX, int gridY) {
    if (isValidGridPosition(gridX, gridY)) {
      dynamicGrid[gridX][gridY] = WALKABLE;
      markForRecalculation();
    }
  }

  public void temporarilyBlockCell(int gridX, int gridY) {
    if (isValidGridPosition(gridX, gridY)) {
      dynamicGrid[gridX][gridY] = BLOCKED;
      markForRecalculation();
    }
  }

  public void temporarilyUnblockArea(int centerX, int centerY, int radius) {
    if (applyAreaState(centerX, centerY, radius, WALKABLE)) {
      markForRecalculation();
    }
  }

  public void temporarilyBlockArea(int centerX, int centerY, int radius) {
    if (applyAreaState(centerX, centerY, radius, BLOCKED)) {
      markForRecalculation();
    }
  }

  private boolean applyAreaState(int centerX, int centerY, int radius, boolean targetState) {
    boolean changed = false;
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = -radius; dy <= radius; dy++) {
        int x = centerX + dx;
        int y = centerY + dy;
        if (isValidGridPosition(x, y) && dynamicGrid[x][y] != targetState) {
          dynamicGrid[x][y] = targetState;
          changed = true;
        }
      }
    }
    return changed;
  }

  private void markCellArea(int gridX, int gridY, int radius, boolean state) {
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = -radius; dy <= radius; dy++) {
        int x = gridX + dx;
        int y = gridY + dy;
        if (isValidGridPosition(x, y)) {
          dynamicGrid[x][y] = state;
        }
      }
    }
  }

  private int clampGridX(int gridX) {
    return Math.max(0, Math.min(gridX, gridWidth - 1));
  }

  private int clampGridY(int gridY) {
    return Math.max(0, Math.min(gridY, gridHeight - 1));
  }

  public int getGridWidth() {
    return gridWidth;
  }

  public int getGridHeight() {
    return gridHeight;
  }

  public float getCellSize() {
    return cellSize;
  }

  public float getOriginX() {
    return originX;
  }

  public float getOriginY() {
    return originY;
  }
}
