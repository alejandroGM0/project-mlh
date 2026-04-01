package io.github.proyectoM.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.pathfinding.NavigationGrid;

/** Renders a visual debug overlay of the navigation grid, paths, and nav-mesh. */
public class NavigationGridDebugRenderer {
  private static final Color WALKABLE_COLOR = new Color(0, 1, 0, 0.4f);
  private static final Color BLOCKED_COLOR = new Color(1, 0, 0, 0.4f);
  private static final Color STATIC_WALKABLE_COLOR = new Color(0, 0.7f, 1, 0.3f);
  private static final Color STATIC_BLOCKED_COLOR = new Color(0.7f, 0, 0.7f, 0.3f);
  private static final Color ENEMY_PATH_COLOR = new Color(1, 0, 0, 0.9f);
  private static final Color COMPANION_PATH_COLOR = new Color(0, 1, 0, 0.9f);
  private static final Color WAYPOINT_COLOR = new Color(1, 1, 0, 0.8f);
  private static final Color GRID_LINES_COLOR = new Color(1, 1, 1, 0.4f);
  private static final float WAYPOINT_RADIUS = 3f;

  private final ShapeRenderer shapeRenderer;
  private final NavigationGrid grid;
  private boolean enabled = false;
  private boolean showGrid = true;
  private boolean showPaths = true;
  private boolean showStaticNavMesh = false;

  public NavigationGridDebugRenderer(NavigationGrid grid) {
    this.grid = grid;
    this.shapeRenderer = new ShapeRenderer();
  }

  /** Renders the debug view of the navigation grid. */
  public void render(OrthographicCamera camera) {
    if (!enabled || grid == null) {
      return;
    }

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    shapeRenderer.setProjectionMatrix(camera.combined);

    if (showGrid) {
      renderGrid();
    }

    if (showStaticNavMesh) {
      renderStaticNavMesh();
    }

    if (showPaths) {
      renderPaths();
    }

    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  /** Renders the grid cells (walkable and blocked). */
  private void renderGrid() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (int x = 0; x < grid.getGridWidth(); x++) {
      for (int y = 0; y < grid.getGridHeight(); y++) {
        float pixelX = x * grid.getCellSize();
        float pixelY = y * grid.getCellSize();

        Color color = grid.isWalkable(x, y) ? WALKABLE_COLOR : BLOCKED_COLOR;
        shapeRenderer.setColor(color);

        shapeRenderer.rect(pixelX, pixelY, grid.getCellSize(), grid.getCellSize());
      }
    }

    shapeRenderer.end();

    renderGridLines();
  }

  /** Renders the grid lines for better visualization. */
  private void renderGridLines() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(GRID_LINES_COLOR);

    float gridWidth = grid.getGridWidth() * grid.getCellSize();
    float gridHeight = grid.getGridHeight() * grid.getCellSize();

    for (int x = 0; x <= grid.getGridWidth(); x++) {
      float pixelX = x * grid.getCellSize();
      shapeRenderer.line(pixelX, 0, pixelX, gridHeight);
    }

    for (int y = 0; y <= grid.getGridHeight(); y++) {
      float pixelY = y * grid.getCellSize();
      shapeRenderer.line(0, pixelY, gridWidth, pixelY);
    }

    shapeRenderer.end();
  }

  /**
   * Renders the active pathfinding paths. (Placeholder for now - will be implemented when we have
   * access to the paths)
   */
  private void renderPaths() {
    // TODO: Implement when we have access to the calculated paths
  }

  /**
   * Renders a specific path.
   *
   * @param path The list of waypoints in the path.
   * @param isEnemy true if the path belongs to an enemy, false for a companion.
   */
  public void renderPath(java.util.List<Vector2> path, boolean isEnemy) {
    if (!enabled || path == null || path.isEmpty()) {
      return;
    }

    Color pathColor = isEnemy ? ENEMY_PATH_COLOR : COMPANION_PATH_COLOR;

    shapeRenderer.setProjectionMatrix(shapeRenderer.getProjectionMatrix());
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    shapeRenderer.setColor(WAYPOINT_COLOR);
    for (Vector2 point : path) {
      float pixelX = point.x;
      float pixelY = point.y;
      shapeRenderer.circle(pixelX, pixelY, WAYPOINT_RADIUS);
    }

    shapeRenderer.end();

    if (path.size() > 1) {
      shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      shapeRenderer.setColor(pathColor);

      for (int i = 0; i < path.size() - 1; i++) {
        Vector2 current = path.get(i);
        Vector2 next = path.get(i + 1);

        if (isLineWalkable(current, next)) {
          shapeRenderer.line(current.x, current.y, next.x, next.y);
        } else {
          drawDashedLine(current, next);
        }
      }

      shapeRenderer.end();
    }
  }

  /** Renders a specific path (legacy version for compatibility). */
  public void renderPath(java.util.List<Vector2> path) {
    renderPath(path, false);
  }

  /**
   * Checks if a line between two points is completely walkable. Uses Bresenham's algorithm to check
   * each cell on the line.
   */
  private boolean isLineWalkable(Vector2 start, Vector2 end) {
    int startX = grid.worldToGridX(start.x);
    int startY = grid.worldToGridY(start.y);
    int endX = grid.worldToGridX(end.x);
    int endY = grid.worldToGridY(end.y);

    return areAllCellsInLineWalkable(startX, startY, endX, endY);
  }

  /** Implementation of Bresenham's algorithm to check walkability on a line. */
  private boolean areAllCellsInLineWalkable(int x0, int y0, int x1, int y1) {
    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);
    int x = x0;
    int y = y0;
    int n = 1 + dx + dy;
    int xInc = (x1 > x0) ? 1 : -1;
    int yInc = (y1 > y0) ? 1 : -1;
    int error = dx - dy;

    dx *= 2;
    dy *= 2;

    for (; n > 0; --n) {
      if (!grid.isValidGridPosition(x, y) || !grid.isWalkable(x, y)) {
        return false;
      }

      if (error > 0) {
        x += xInc;
        error -= dy;
      } else {
        y += yInc;
        error += dx;
      }
    }

    return true;
  }

  /** Draws a dashed line to indicate paths that cross obstacles. */
  private void drawDashedLine(Vector2 start, Vector2 end) {
    Color originalColor = shapeRenderer.getColor();
    shapeRenderer.setColor(1, 0.5f, 0, 0.8f);

    float dx = end.x - start.x;
    float dy = end.y - start.y;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);

    float dashLength = 5f;
    float gapLength = 3f;
    float segmentLength = dashLength + gapLength;
    int numSegments = (int) (distance / segmentLength);

    for (int i = 0; i < numSegments; i++) {
      float t1 = (i * segmentLength) / distance;
      float t2 = Math.min(((i * segmentLength) + dashLength) / distance, 1.0f);

      float x1 = start.x + dx * t1;
      float y1 = start.y + dy * t1;
      float x2 = start.x + dx * t2;
      float y2 = start.y + dy * t2;

      shapeRenderer.line(x1, y1, x2, y2);
    }

    shapeRenderer.setColor(originalColor);
  }

  /** Enables or disables the visual debug. */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** Controls whether the grid is shown. */
  public void setShowGrid(boolean showGrid) {
    this.showGrid = showGrid;
  }

  /** Controls whether the paths are shown. */
  public void setShowPaths(boolean showPaths) {
    this.showPaths = showPaths;
  }

  /** Controls whether the static navmesh (only static obstacles) is shown. */
  public void setShowStaticNavMesh(boolean showStaticNavMesh) {
    this.showStaticNavMesh = showStaticNavMesh;
  }

  /** Gets the enabled state of the debug. */
  public boolean isEnabled() {
    return enabled;
  }

  /** Renders the static navmesh (only static obstacles from the base map). */
  private void renderStaticNavMesh() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (int x = 0; x < grid.getGridWidth(); x++) {
      for (int y = 0; y < grid.getGridHeight(); y++) {
        float pixelX = x * grid.getCellSize();
        float pixelY = y * grid.getCellSize();

        Color color =
            grid.isWalkableStaticOnly(x, y) ? STATIC_WALKABLE_COLOR : STATIC_BLOCKED_COLOR;
        shapeRenderer.setColor(color);

        shapeRenderer.rect(pixelX, pixelY, grid.getCellSize(), grid.getCellSize());
      }
    }

    shapeRenderer.end();

    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(GRID_LINES_COLOR);

    float gridWidth = grid.getGridWidth() * grid.getCellSize();
    float gridHeight = grid.getGridHeight() * grid.getCellSize();

    for (int x = 0; x <= grid.getGridWidth(); x++) {
      float pixelX = x * grid.getCellSize();
      shapeRenderer.line(pixelX, 0, pixelX, gridHeight);
    }

    for (int y = 0; y <= grid.getGridHeight(); y++) {
      float pixelY = y * grid.getCellSize();
      shapeRenderer.line(0, pixelY, gridWidth, pixelY);
    }

    shapeRenderer.end();
  }

  /** Releases the resources used. */
  public void dispose() {
    shapeRenderer.dispose();
  }
}
