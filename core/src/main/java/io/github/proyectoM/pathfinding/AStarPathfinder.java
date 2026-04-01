package io.github.proyectoM.pathfinding;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/** Finds grid paths using A* with optional static-only obstacle checks. */
public class AStarPathfinder {
  private static final float ZERO_COST = 0f;
  private static final int ZERO_OFFSET = 0;
  private static final int SINGLE_STEP = 1;
  private static final int MAX_ITERATIONS = 10_000;
  private static final String NODE_KEY_SEPARATOR = ",";

  private static final float MIN_OBSTACLE_DISTANCE = 2.0f;
  private static final float OBSTACLE_PENALTY_SCALE = 0.1f;
  private static final float MAX_OBSTACLE_PENALTY = 1.0f;
  private static final int OBSTACLE_RADIUS_BUFFER_CELLS = 1;
  private static final int MIN_OBSTACLE_CHECK_RADIUS = 1;

  private static final int MIN_PATH_POINTS_FOR_OPTIMIZATION = 2;
  private static final int LINE_OF_SIGHT_RADIUS_CELLS = 1;
  private static final float DIRECT_LINE_OF_SIGHT_DISTANCE = 1.5f;
  private static final int BRESENHAM_STEP_SCALE = 2;

  private static final float STRAIGHT_COST = 1.0f;
  private static final float DIAGONAL_COST = 1.414f;

  private static final int[] NORTH = {ZERO_OFFSET, SINGLE_STEP};
  private static final int[] NORTHEAST = {SINGLE_STEP, SINGLE_STEP};
  private static final int[] EAST = {SINGLE_STEP, ZERO_OFFSET};
  private static final int[] SOUTHEAST = {SINGLE_STEP, -SINGLE_STEP};
  private static final int[] SOUTH = {ZERO_OFFSET, -SINGLE_STEP};
  private static final int[] SOUTHWEST = {-SINGLE_STEP, -SINGLE_STEP};
  private static final int[] WEST = {-SINGLE_STEP, ZERO_OFFSET};
  private static final int[] NORTHWEST = {-SINGLE_STEP, SINGLE_STEP};

  private static final int[][] DIRECTIONS = {
    NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST
  };

  private static final Comparator<Node> NODE_COMPARATOR =
      (firstNode, secondNode) -> {
        int fCostComparison = Float.compare(firstNode.fCost, secondNode.fCost);
        if (fCostComparison != 0) {
          return fCostComparison;
        }
        return Float.compare(firstNode.hCost, secondNode.hCost);
      };

  /**
   * Finds a path from the start point to the goal using A*.
   *
   * @param grid navigation grid
   * @param startX initial x coordinate
   * @param startY initial y coordinate
   * @param goalX target x coordinate
   * @param goalY target y coordinate
   * @return path waypoints or {@code null} when no path is found
   */
  public Queue<Vector2> findPath(
      NavigationGrid grid, int startX, int startY, int goalX, int goalY) {
    return findPath(grid, startX, startY, goalX, goalY, ZERO_COST);
  }

  /**
   * Finds a path from the start point to the goal using A*.
   *
   * @param grid navigation grid
   * @param startX initial x coordinate
   * @param startY initial y coordinate
   * @param goalX target x coordinate
   * @param goalY target y coordinate
   * @param agentRadiusPixels agent radius in pixels
   * @return path waypoints or {@code null} when no path is found
   */
  public Queue<Vector2> findPath(
      NavigationGrid grid, int startX, int startY, int goalX, int goalY, float agentRadiusPixels) {
    if (!canSearch(grid, startX, startY, goalX, goalY, agentRadiusPixels, false)) {
      return null;
    }

    if (startX == goalX && startY == goalY) {
      return createSinglePointPath(goalX, goalY);
    }

    return executeSearch(grid, startX, startY, goalX, goalY, agentRadiusPixels, false);
  }

  /**
   * Finds a path ignoring dynamic obstacles and using only static walkability.
   *
   * @param grid navigation grid
   * @param startX initial x coordinate
   * @param startY initial y coordinate
   * @param goalX target x coordinate
   * @param goalY target y coordinate
   * @param agentRadiusPixels agent radius in pixels
   * @return path waypoints or {@code null} when no path is found
   */
  public Queue<Vector2> findPathStaticOnly(
      NavigationGrid grid, int startX, int startY, int goalX, int goalY, float agentRadiusPixels) {
    if (!canSearch(grid, startX, startY, goalX, goalY, agentRadiusPixels, true)) {
      return null;
    }

    if (startX == goalX && startY == goalY) {
      return createSinglePointPath(goalX, goalY);
    }

    return executeSearch(grid, startX, startY, goalX, goalY, agentRadiusPixels, true);
  }

  private boolean canSearch(
      NavigationGrid grid,
      int startX,
      int startY,
      int goalX,
      int goalY,
      float agentRadiusPixels,
      boolean staticOnly) {
    if (grid == null) {
      return false;
    }

    if (!grid.isValidGridPosition(startX, startY) || !grid.isValidGridPosition(goalX, goalY)) {
      return false;
    }

    return isWalkable(grid, startX, startY, agentRadiusPixels, staticOnly)
        && isWalkable(grid, goalX, goalY, agentRadiusPixels, staticOnly);
  }

  private Queue<Vector2> createSinglePointPath(int goalX, int goalY) {
    Queue<Vector2> path = new LinkedList<>();
    path.offer(new Vector2(goalX, goalY));
    return path;
  }

  private Queue<Vector2> executeSearch(
      NavigationGrid grid,
      int startX,
      int startY,
      int goalX,
      int goalY,
      float agentRadiusPixels,
      boolean staticOnly) {
    PriorityQueue<Node> openSet = new PriorityQueue<>(NODE_COMPARATOR);
    Set<Node> closedSet = new HashSet<>();
    Map<String, Node> nodeMap = new HashMap<>();

    Node startNode = createStartNode(startX, startY, goalX, goalY);
    openSet.offer(startNode);
    nodeMap.put(getNodeKey(startX, startY), startNode);

    int iterations = ZERO_OFFSET;
    while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
      iterations++;

      Node currentNode = openSet.poll();
      closedSet.add(currentNode);

      if (currentNode.x == goalX && currentNode.y == goalY) {
        return reconstructPath(grid, currentNode);
      }

      exploreNeighbors(
          grid,
          currentNode,
          goalX,
          goalY,
          openSet,
          closedSet,
          nodeMap,
          agentRadiusPixels,
          staticOnly);
    }

    return null;
  }

  private Node createStartNode(int startX, int startY, int goalX, int goalY) {
    Node startNode = new Node(startX, startY);
    startNode.gCost = ZERO_COST;
    startNode.hCost = calculateHeuristic(startX, startY, goalX, goalY);
    startNode.calculateFCost();
    return startNode;
  }

  private void exploreNeighbors(
      NavigationGrid grid,
      Node currentNode,
      int goalX,
      int goalY,
      PriorityQueue<Node> openSet,
      Set<Node> closedSet,
      Map<String, Node> nodeMap,
      float agentRadiusPixels,
      boolean staticOnly) {
    for (int[] direction : DIRECTIONS) {
      int neighborX = currentNode.x + direction[0];
      int neighborY = currentNode.y + direction[1];

      if (!isWalkable(grid, neighborX, neighborY, agentRadiusPixels, staticOnly)) {
        continue;
      }

      boolean diagonalMove = isDiagonal(direction);
      if (!staticOnly
          && diagonalMove
          && !isDiagonalMovementSafe(
              grid, currentNode.x, currentNode.y, neighborX, neighborY, agentRadiusPixels)) {
        continue;
      }

      Node neighborNode = getOrCreateNode(neighborX, neighborY, nodeMap);
      if (closedSet.contains(neighborNode)) {
        continue;
      }

      float movementCost = diagonalMove ? DIAGONAL_COST : STRAIGHT_COST;
      if (!staticOnly) {
        movementCost += calculateObstaclePenalty(grid, neighborX, neighborY, agentRadiusPixels);
      }

      float tentativeGCost = currentNode.gCost + movementCost;
      updateOpenSet(openSet, currentNode, goalX, goalY, neighborNode, tentativeGCost, staticOnly);
    }
  }

  private boolean isWalkable(
      NavigationGrid grid, int gridX, int gridY, float agentRadiusPixels, boolean staticOnly) {
    if (!grid.isValidGridPosition(gridX, gridY)) {
      return false;
    }

    if (staticOnly) {
      return grid.isWalkableWithRadiusStaticOnly(gridX, gridY, agentRadiusPixels);
    }

    return grid.isWalkableWithRadius(gridX, gridY, agentRadiusPixels);
  }

  private boolean isDiagonal(int[] direction) {
    return direction[0] != ZERO_OFFSET && direction[1] != ZERO_OFFSET;
  }

  private void updateOpenSet(
      PriorityQueue<Node> openSet,
      Node currentNode,
      int goalX,
      int goalY,
      Node neighborNode,
      float tentativeGCost,
      boolean forceReinsert) {
    if (!openSet.contains(neighborNode)) {
      assignCosts(currentNode, goalX, goalY, neighborNode, tentativeGCost);
      openSet.offer(neighborNode);
      return;
    }

    if (tentativeGCost < neighborNode.gCost) {
      assignCosts(currentNode, goalX, goalY, neighborNode, tentativeGCost);
      if (forceReinsert) {
        openSet.remove(neighborNode);
        openSet.offer(neighborNode);
      }
    }
  }

  private void assignCosts(
      Node currentNode, int goalX, int goalY, Node neighborNode, float tentativeGCost) {
    neighborNode.parent = currentNode;
    neighborNode.gCost = tentativeGCost;
    neighborNode.hCost = calculateHeuristic(neighborNode.x, neighborNode.y, goalX, goalY);
    neighborNode.calculateFCost();
  }

  private boolean isDiagonalMovementSafe(
      NavigationGrid grid, int fromX, int fromY, int toX, int toY, float agentRadiusPixels) {
    int deltaX = toX - fromX;
    int deltaY = toY - fromY;

    boolean firstSideClear = grid.isWalkableWithRadius(fromX + deltaX, fromY, agentRadiusPixels);
    boolean secondSideClear = grid.isWalkableWithRadius(fromX, fromY + deltaY, agentRadiusPixels);
    return firstSideClear || secondSideClear;
  }

  private float calculateObstaclePenalty(
      NavigationGrid grid, int x, int y, float agentRadiusPixels) {
    float penalty = ZERO_COST;
    int checkRadius =
        Math.max(MIN_OBSTACLE_CHECK_RADIUS, (int) (agentRadiusPixels / grid.getCellSize()))
            + OBSTACLE_RADIUS_BUFFER_CELLS;

    for (int dx = -checkRadius; dx <= checkRadius; dx++) {
      for (int dy = -checkRadius; dy <= checkRadius; dy++) {
        if (dx == ZERO_OFFSET && dy == ZERO_OFFSET) {
          continue;
        }

        int checkX = x + dx;
        int checkY = y + dy;
        if (!grid.isValidGridPosition(checkX, checkY) || !grid.isWalkable(checkX, checkY)) {
          float distance = (float) Math.sqrt(dx * dx + dy * dy);
          penalty += (MIN_OBSTACLE_DISTANCE - distance) * OBSTACLE_PENALTY_SCALE;
        }
      }
    }

    return Math.min(penalty, MAX_OBSTACLE_PENALTY);
  }

  private Node getOrCreateNode(int x, int y, Map<String, Node> nodeMap) {
    return nodeMap.computeIfAbsent(getNodeKey(x, y), key -> new Node(x, y));
  }

  private String getNodeKey(int x, int y) {
    return x + NODE_KEY_SEPARATOR + y;
  }

  private float calculateHeuristic(int x1, int y1, int x2, int y2) {
    return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
  }

  private Queue<Vector2> reconstructPath(NavigationGrid grid, Node goalNode) {
    List<Vector2> path = new ArrayList<>();
    Node currentNode = goalNode;

    while (currentNode != null) {
      path.add(new Vector2(currentNode.x, currentNode.y));
      currentNode = currentNode.parent;
    }

    Collections.reverse(path);
    return new LinkedList<>(optimizePath(grid, path));
  }

  private List<Vector2> optimizePath(NavigationGrid grid, List<Vector2> originalPath) {
    if (originalPath.size() <= MIN_PATH_POINTS_FOR_OPTIMIZATION) {
      return originalPath;
    }

    List<Vector2> optimizedPath = new ArrayList<>();
    optimizedPath.add(originalPath.get(ZERO_OFFSET));

    int currentIndex = ZERO_OFFSET;
    while (currentIndex < originalPath.size() - SINGLE_STEP) {
      int farthestIndex = currentIndex + SINGLE_STEP;

      for (int index = currentIndex + 2; index < originalPath.size(); index++) {
        Vector2 current = originalPath.get(currentIndex);
        Vector2 target = originalPath.get(index);
        if (hasLineOfSight(grid, current, target)) {
          farthestIndex = index;
        } else {
          break;
        }
      }

      optimizedPath.add(originalPath.get(farthestIndex));
      currentIndex = farthestIndex;
    }

    return optimizedPath;
  }

  private boolean hasLineOfSight(NavigationGrid grid, Vector2 from, Vector2 to) {
    int x0 = (int) from.x;
    int y0 = (int) from.y;
    int x1 = (int) to.x;
    int y1 = (int) to.y;

    if (!isLineWalkableWithRadius(grid, x0, y0, x1, y1, LINE_OF_SIGHT_RADIUS_CELLS)) {
      return false;
    }

    float distance = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
    return distance <= DIRECT_LINE_OF_SIGHT_DISTANCE;
  }

  private boolean isLineWalkableWithRadius(
      NavigationGrid grid, int x0, int y0, int x1, int y1, int radius) {
    int dx = x1 - x0;
    int dy = y1 - y0;

    int perpendicularX = -dy;
    int perpendicularY = dx;

    float length =
        (float) Math.sqrt(perpendicularX * perpendicularX + perpendicularY * perpendicularY);
    if (length > ZERO_COST) {
      perpendicularX = Math.round(perpendicularX / length);
      perpendicularY = Math.round(perpendicularY / length);
    }

    for (int offset = -radius; offset <= radius; offset++) {
      int startX = x0 + offset * perpendicularX;
      int startY = y0 + offset * perpendicularY;
      int endX = x1 + offset * perpendicularX;
      int endY = y1 + offset * perpendicularY;

      if (!isLineWalkableInGrid(grid, startX, startY, endX, endY)) {
        return false;
      }
    }

    return true;
  }

  private boolean isLineWalkableInGrid(NavigationGrid grid, int x0, int y0, int x1, int y1) {
    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);
    int x = x0;
    int y = y0;
    int pointCount = SINGLE_STEP + dx + dy;
    int xIncrement = x1 > x0 ? SINGLE_STEP : -SINGLE_STEP;
    int yIncrement = y1 > y0 ? SINGLE_STEP : -SINGLE_STEP;
    int error = dx - dy;

    dx *= BRESENHAM_STEP_SCALE;
    dy *= BRESENHAM_STEP_SCALE;

    for (; pointCount > ZERO_OFFSET; pointCount--) {
      if (!grid.isValidGridPosition(x, y) || !grid.isWalkable(x, y)) {
        return false;
      }

      if (error > ZERO_OFFSET) {
        x += xIncrement;
        error -= dy;
      } else {
        y += yIncrement;
        error += dx;
      }
    }

    return true;
  }

  /** Internal node used by the A* search. */
  private static class Node {
    private final int x;
    private final int y;
    private float gCost;
    private float hCost;
    private float fCost;
    private Node parent;

    private Node(int x, int y) {
      this.x = x;
      this.y = y;
      this.gCost = ZERO_COST;
      this.hCost = ZERO_COST;
      this.fCost = ZERO_COST;
    }

    private void calculateFCost() {
      fCost = gCost + hCost;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other == null || getClass() != other.getClass()) {
        return false;
      }

      Node node = (Node) other;
      return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }
}
