package io.github.proyectoM.pathfinding;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/** Finds grid paths using the Jump Point Search optimization over uniform grids. */
public class JPSPathfinder {
  private static final int ZERO_COST = 0;
  private static final int NO_MOVEMENT = 0;
  private static final int POSITIVE_STEP = 1;
  private static final int NEGATIVE_STEP = -1;
  private static final int NODE_KEY_BIT_SHIFT = 16;
  private static final int NODE_KEY_Y_MASK = 0xFFFF;
  private static final float DIAGONAL_COST = 1.414f;
  private static final float ORTHOGONAL_COST = 1.0f;

  private static final int[] NORTH = {NO_MOVEMENT, NEGATIVE_STEP};
  private static final int[] EAST = {POSITIVE_STEP, NO_MOVEMENT};
  private static final int[] SOUTH = {NO_MOVEMENT, POSITIVE_STEP};
  private static final int[] WEST = {NEGATIVE_STEP, NO_MOVEMENT};
  private static final int[] NORTHEAST = {POSITIVE_STEP, NEGATIVE_STEP};
  private static final int[] SOUTHEAST = {POSITIVE_STEP, POSITIVE_STEP};
  private static final int[] SOUTHWEST = {NEGATIVE_STEP, POSITIVE_STEP};
  private static final int[] NORTHWEST = {NEGATIVE_STEP, NEGATIVE_STEP};

  private static final int[][] DIRECTIONS = {
    NORTH, EAST, SOUTH, WEST, NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST
  };

  /**
   * Finds a path using Jump Point Search.
   *
   * @param grid navigation grid
   * @param startX initial x coordinate
   * @param startY initial y coordinate
   * @param goalX target x coordinate
   * @param goalY target y coordinate
   * @param agentRadiusPixels agent radius in pixels
   * @return waypoints in grid space, or {@code null} when no path is found
   */
  public Queue<Vector2> findPath(
      NavigationGrid grid, int startX, int startY, int goalX, int goalY, float agentRadiusPixels) {
    if (!grid.isValidGridPosition(startX, startY) || !grid.isValidGridPosition(goalX, goalY)) {
      return null;
    }

    if (!grid.isWalkableWithRadius(goalX, goalY, agentRadiusPixels)) {
      return null;
    }

    if (startX == goalX && startY == goalY) {
      Queue<Vector2> path = new LinkedList<>();
      path.offer(new Vector2(startX, startY));
      return path;
    }

    PriorityQueue<JpsNode> openSet = new PriorityQueue<>();
    Set<Integer> closedSet = new HashSet<>();
    Map<Integer, JpsNode> allNodes = new HashMap<>();

    JpsNode startNode = new JpsNode(startX, startY);
    startNode.g = ZERO_COST;
    startNode.h = heuristic(startX, startY, goalX, goalY);
    startNode.f = startNode.h;

    openSet.offer(startNode);
    allNodes.put(getNodeKey(startX, startY), startNode);

    while (!openSet.isEmpty()) {
      JpsNode current = openSet.poll();

      if (current.x == goalX && current.y == goalY) {
        return reconstructPath(current);
      }

      int currentKey = getNodeKey(current.x, current.y);
      if (closedSet.contains(currentKey)) {
        continue;
      }
      closedSet.add(currentKey);

      List<JpsNode> successors = identifySuccessors(grid, current, goalX, goalY, agentRadiusPixels);

      for (JpsNode successor : successors) {
        int successorKey = getNodeKey(successor.x, successor.y);
        if (closedSet.contains(successorKey)) {
          continue;
        }

        float tentativeG = current.g + distance(current.x, current.y, successor.x, successor.y);
        JpsNode existingNode = allNodes.get(successorKey);
        if (existingNode == null || tentativeG < existingNode.g) {
          successor.g = tentativeG;
          successor.h = heuristic(successor.x, successor.y, goalX, goalY);
          successor.f = successor.g + successor.h;
          successor.parent = current;

          allNodes.put(successorKey, successor);
          openSet.offer(successor);
        }
      }
    }

    return null;
  }

  private List<JpsNode> identifySuccessors(
      NavigationGrid grid, JpsNode current, int goalX, int goalY, float agentRadiusPixels) {
    List<JpsNode> successors = new ArrayList<>();
    List<int[]> neighbors = findNeighbors(grid, current, agentRadiusPixels);

    for (int[] direction : neighbors) {
      JpsNode jumpPoint =
          jump(
              grid,
              current.x,
              current.y,
              direction[0],
              direction[1],
              goalX,
              goalY,
              agentRadiusPixels);
      if (jumpPoint != null) {
        successors.add(jumpPoint);
      }
    }

    return successors;
  }

  private JpsNode jump(
      NavigationGrid grid,
      int x,
      int y,
      int dx,
      int dy,
      int goalX,
      int goalY,
      float agentRadiusPixels) {
    int nextX = x + dx;
    int nextY = y + dy;

    if (!grid.isValidGridPosition(nextX, nextY)
        || !grid.isWalkableWithRadius(nextX, nextY, agentRadiusPixels)) {
      return null;
    }

    if (nextX == goalX && nextY == goalY) {
      return new JpsNode(nextX, nextY);
    }

    if (isDiagonalMove(dx, dy)) {
      if (hasForcedNeighborOnDiagonal(grid, nextX, nextY, dx, dy, agentRadiusPixels)) {
        return new JpsNode(nextX, nextY);
      }

      if (jump(grid, nextX, nextY, dx, NO_MOVEMENT, goalX, goalY, agentRadiusPixels) != null
          || jump(grid, nextX, nextY, NO_MOVEMENT, dy, goalX, goalY, agentRadiusPixels) != null) {
        return new JpsNode(nextX, nextY);
      }
    } else if (hasForcedNeighborOnStraight(grid, nextX, nextY, dx, dy, agentRadiusPixels)) {
      return new JpsNode(nextX, nextY);
    }

    return jump(grid, nextX, nextY, dx, dy, goalX, goalY, agentRadiusPixels);
  }

  private boolean isDiagonalMove(int dx, int dy) {
    return dx != NO_MOVEMENT && dy != NO_MOVEMENT;
  }

  private boolean hasForcedNeighborOnDiagonal(
      NavigationGrid grid, int nextX, int nextY, int dx, int dy, float agentRadiusPixels) {
    return (isWalkable(grid, nextX - dx, nextY, agentRadiusPixels)
            && !grid.isWalkableWithRadius(nextX - dx, nextY - dy, agentRadiusPixels))
        || (isWalkable(grid, nextX, nextY - dy, agentRadiusPixels)
            && !grid.isWalkableWithRadius(nextX - dx, nextY - dy, agentRadiusPixels));
  }

  private boolean hasForcedNeighborOnStraight(
      NavigationGrid grid, int nextX, int nextY, int dx, int dy, float agentRadiusPixels) {
    if (dx != NO_MOVEMENT) {
      return (isWalkable(grid, nextX, nextY + POSITIVE_STEP, agentRadiusPixels)
              && isValidGridPosition(grid, nextX - dx, nextY + POSITIVE_STEP)
              && !grid.isWalkableWithRadius(nextX - dx, nextY + POSITIVE_STEP, agentRadiusPixels))
          || (isWalkable(grid, nextX, nextY + NEGATIVE_STEP, agentRadiusPixels)
              && isValidGridPosition(grid, nextX - dx, nextY + NEGATIVE_STEP)
              && !grid.isWalkableWithRadius(nextX - dx, nextY + NEGATIVE_STEP, agentRadiusPixels));
    }

    return (isWalkable(grid, nextX + POSITIVE_STEP, nextY, agentRadiusPixels)
            && isValidGridPosition(grid, nextX + POSITIVE_STEP, nextY - dy)
            && !grid.isWalkableWithRadius(nextX + POSITIVE_STEP, nextY - dy, agentRadiusPixels))
        || (isWalkable(grid, nextX + NEGATIVE_STEP, nextY, agentRadiusPixels)
            && isValidGridPosition(grid, nextX + NEGATIVE_STEP, nextY - dy)
            && !grid.isWalkableWithRadius(nextX + NEGATIVE_STEP, nextY - dy, agentRadiusPixels));
  }

  private boolean isWalkable(NavigationGrid grid, int gridX, int gridY, float agentRadiusPixels) {
    return isValidGridPosition(grid, gridX, gridY)
        && grid.isWalkableWithRadius(gridX, gridY, agentRadiusPixels);
  }

  private boolean isValidGridPosition(NavigationGrid grid, int gridX, int gridY) {
    return grid.isValidGridPosition(gridX, gridY);
  }

  private List<int[]> findNeighbors(NavigationGrid grid, JpsNode node, float agentRadiusPixels) {
    List<int[]> neighbors = new ArrayList<>();

    if (node.parent == null) {
      for (int[] direction : DIRECTIONS) {
        int neighborX = node.x + direction[0];
        int neighborY = node.y + direction[1];
        if (isWalkable(grid, neighborX, neighborY, agentRadiusPixels)) {
          neighbors.add(direction);
        }
      }
      return neighbors;
    }

    int dx = clampDirection(node.x - node.parent.x);
    int dy = clampDirection(node.y - node.parent.y);

    if (isDiagonalMove(dx, dy)) {
      addIfWalkable(neighbors, grid, node.x, node.y + dy, agentRadiusPixels, NO_MOVEMENT, dy);
      addIfWalkable(neighbors, grid, node.x + dx, node.y, agentRadiusPixels, dx, NO_MOVEMENT);
      addIfWalkable(neighbors, grid, node.x + dx, node.y + dy, agentRadiusPixels, dx, dy);
      return neighbors;
    }

    if (dx != NO_MOVEMENT) {
      addHorizontalNeighbors(neighbors, grid, node, dx, agentRadiusPixels);
      return neighbors;
    }

    addVerticalNeighbors(neighbors, grid, node, dy, agentRadiusPixels);
    return neighbors;
  }

  private void addHorizontalNeighbors(
      List<int[]> neighbors, NavigationGrid grid, JpsNode node, int dx, float agentRadiusPixels) {
    if (!grid.isWalkableWithRadius(node.x + dx, node.y, agentRadiusPixels)) {
      return;
    }

    neighbors.add(direction(dx, NO_MOVEMENT));
    if (!grid.isWalkableWithRadius(node.x, node.y + POSITIVE_STEP, agentRadiusPixels)) {
      neighbors.add(direction(dx, POSITIVE_STEP));
    }
    if (!grid.isWalkableWithRadius(node.x, node.y + NEGATIVE_STEP, agentRadiusPixels)) {
      neighbors.add(direction(dx, NEGATIVE_STEP));
    }
  }

  private void addVerticalNeighbors(
      List<int[]> neighbors, NavigationGrid grid, JpsNode node, int dy, float agentRadiusPixels) {
    if (!grid.isWalkableWithRadius(node.x, node.y + dy, agentRadiusPixels)) {
      return;
    }

    neighbors.add(direction(NO_MOVEMENT, dy));
    if (!grid.isWalkableWithRadius(node.x + POSITIVE_STEP, node.y, agentRadiusPixels)) {
      neighbors.add(direction(POSITIVE_STEP, dy));
    }
    if (!grid.isWalkableWithRadius(node.x + NEGATIVE_STEP, node.y, agentRadiusPixels)) {
      neighbors.add(direction(NEGATIVE_STEP, dy));
    }
  }

  private void addIfWalkable(
      List<int[]> neighbors,
      NavigationGrid grid,
      int gridX,
      int gridY,
      float agentRadiusPixels,
      int dx,
      int dy) {
    if (grid.isWalkableWithRadius(gridX, gridY, agentRadiusPixels)) {
      neighbors.add(direction(dx, dy));
    }
  }

  private int[] direction(int dx, int dy) {
    return new int[] {dx, dy};
  }

  private Queue<Vector2> reconstructPath(JpsNode goalNode) {
    LinkedList<Vector2> path = new LinkedList<>();
    JpsNode current = goalNode;

    while (current != null) {
      path.addFirst(new Vector2(current.x, current.y));
      current = current.parent;
    }

    return path;
  }

  private float heuristic(int x1, int y1, int x2, int y2) {
    int dx = Math.abs(x2 - x1);
    int dy = Math.abs(y2 - y1);
    return (dx + dy) * ORTHOGONAL_COST + (DIAGONAL_COST - (ORTHOGONAL_COST * 2)) * Math.min(dx, dy);
  }

  private float distance(int x1, int y1, int x2, int y2) {
    int dx = x2 - x1;
    int dy = y2 - y1;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  private int clampDirection(int value) {
    return Integer.compare(value, ZERO_COST);
  }

  private int getNodeKey(int x, int y) {
    return (x << NODE_KEY_BIT_SHIFT) | (y & NODE_KEY_Y_MASK);
  }

  private static class JpsNode implements Comparable<JpsNode> {
    private final int x;
    private final int y;
    private float g;
    private float h;
    private float f;
    private JpsNode parent;

    private JpsNode(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public int compareTo(JpsNode other) {
      return Float.compare(f, other.f);
    }
  }
}
