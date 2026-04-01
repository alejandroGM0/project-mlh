package io.github.proyectoM.systems.enemy.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.movement.PathfindingComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.pathfinding.AStarPathfinder;
import io.github.proyectoM.pathfinding.NavigationGrid;
import io.github.proyectoM.physics.PhysicsConstants;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/** A system that calculates and updates navigation paths specifically for enemies. */
public class EnemyPathfindingSystem extends EntitySystem {
  private static final float DEFAULT_AGENT_RADIUS_PIXELS = 50f;
  private static final float DEFAULT_RADIUS_METERS = 0.5f;
  private static final int WAYPOINTS_TO_KEEP = 1;
  private static final int MIN_PATH_POINTS_TO_SMOOTH = 2;
  private static final int MIN_LINE_OF_SIGHT_SAMPLES = 2;
  private static final int PATH_SMOOTHING_LOOKAHEAD = 5;

  private final NavigationGrid navigationGrid;
  private final AStarPathfinder pathfinder;

  private final ComponentMapper<PathfindingComponent> pathfindingMapper =
      ComponentMapper.getFor(PathfindingComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);

  private ImmutableArray<Entity> enemyEntities;

  public EnemyPathfindingSystem(NavigationGrid navigationGrid) {
    this.navigationGrid = Objects.requireNonNull(navigationGrid, "navigationGrid");
    this.pathfinder = new AStarPathfinder();
  }

  @Override
  public void addedToEngine(Engine engine) {
    enemyEntities =
        engine.getEntitiesFor(
            Family.all(EnemyComponent.class, PathfindingComponent.class, PositionComponent.class)
                .get());
  }

  @Override
  public void update(float deltaTime) {
    for (int i = 0; i < enemyEntities.size(); i++) {
      Entity entity = enemyEntities.get(i);
      updateEnemyPathfinding(entity, deltaTime);
    }
  }

  private void updateEnemyPathfinding(Entity entity, float deltaTime) {
    PathfindingComponent pathfinding = pathfindingMapper.get(entity);
    PositionComponent position = positionMapper.get(entity);

    if (shouldRecalculatePath(pathfinding, deltaTime)) {
      calculateEnemyPath(entity, pathfinding, position);
    }

    processPathMovement(pathfinding, position);
  }

  private boolean shouldRecalculatePath(PathfindingComponent pathfinding, float deltaTime) {
    if (!pathfinding.updateCooldownTimer(deltaTime)) {
      return false;
    }

    if (pathfinding.needsNewPath || !pathfinding.hasValidPath) {
      return true;
    }

    return pathfinding.shouldRecalculateForTargetMovement();
  }

  private void calculateEnemyPath(
      Entity entity, PathfindingComponent pathfinding, PositionComponent position) {
    float agentRadius = getAgentRadius(entity);
    LinkedList<Vector2> waypointsToKeep = getWaypointsToKeep(pathfinding);
    Vector2 recalculateFrom = waypointsToKeep.isEmpty() ? null : waypointsToKeep.getLast();

    int startX = getStartGridX(position, recalculateFrom);
    int startY = getStartGridY(position, recalculateFrom);

    int goalX = navigationGrid.worldToGridX(pathfinding.targetPosition.x);
    int goalY = navigationGrid.worldToGridY(pathfinding.targetPosition.y);

    if (!navigationGrid.isValidGridPosition(startX, startY)
        || !navigationGrid.isValidGridPosition(goalX, goalY)) {
      pathfinding.clearPath();
      return;
    }

    Queue<Vector2> gridPath =
        pathfinder.findPathStaticOnly(navigationGrid, startX, startY, goalX, goalY, agentRadius);

    if (gridPath != null && !gridPath.isEmpty()) {
      if (recalculateFrom != null) {
        gridPath.poll();
      }

      Queue<Vector2> worldPath = convertGridPathToWorldPath(gridPath);
      LinkedList<Vector2> finalPath = mergePathSegments(waypointsToKeep, worldPath);
      smoothPath(finalPath, agentRadius);
      pathfinding.setPath(finalPath);
      pathfinding.markPathCalculationSuccess();
      pathfinding.resetCooldown();
    } else {
      pathfinding.clearPath();
    }
  }

  private void smoothPath(LinkedList<Vector2> path, float agentRadius) {
    if (path.size() <= MIN_PATH_POINTS_TO_SMOOTH) {
      return;
    }

    int currentIndex = 2;

    while (currentIndex < path.size() - 1) {
      Vector2 currentPoint = path.get(currentIndex);
      boolean foundShortcut = false;

      int maxLookahead = Math.min(currentIndex + PATH_SMOOTHING_LOOKAHEAD, path.size() - 1);

      for (int lookaheadIndex = maxLookahead; lookaheadIndex > currentIndex + 1; lookaheadIndex--) {
        Vector2 targetPoint = path.get(lookaheadIndex);

        if (hasLineOfSight(currentPoint, targetPoint, agentRadius)) {
          removeWaypointsBetween(path, currentIndex, lookaheadIndex);
          foundShortcut = true;
          break;
        }
      }

      if (!foundShortcut) {
        currentIndex++;
      }
    }
  }

  private void removeWaypointsBetween(LinkedList<Vector2> path, int startIndex, int endIndex) {
    int toRemove = endIndex - startIndex - 1;

    for (int i = 0; i < toRemove; i++) {
      path.remove(startIndex + 1);
    }
  }

  private boolean hasLineOfSight(Vector2 from, Vector2 to, float agentRadius) {
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);

    int samples =
        Math.max(MIN_LINE_OF_SIGHT_SAMPLES, (int) (distance / navigationGrid.getCellSize()));

    for (int i = 0; i <= samples; i++) {
      float t = i / (float) samples;
      float x = from.x + dx * t;
      float y = from.y + dy * t;

      int gridX = navigationGrid.worldToGridX(x);
      int gridY = navigationGrid.worldToGridY(y);

      if (!navigationGrid.isValidGridPosition(gridX, gridY)) {
        return false;
      }

      if (!navigationGrid.isWalkableWithRadiusStaticOnly(gridX, gridY, agentRadius)) {
        return false;
      }
    }

    return true;
  }

  private float getAgentRadius(Entity entity) {
    PhysicsComponent physics = physicsMapper.get(entity);

    if (physics == null || physics.body == null) {
      return DEFAULT_AGENT_RADIUS_PIXELS;
    }

    Fixture fixture = physics.body.getFixtureList().first();
    if (fixture == null) {
      return DEFAULT_AGENT_RADIUS_PIXELS;
    }

    Shape shape = fixture.getShape();
    float radiusMeters = DEFAULT_RADIUS_METERS;

    if (shape instanceof CircleShape) {
      radiusMeters = shape.getRadius();
    } else if (shape instanceof PolygonShape) {
      PolygonShape polygon = (PolygonShape) shape;
      float maxDistance = 0f;
      Vector2 vertex = new Vector2();

      for (int i = 0; i < polygon.getVertexCount(); i++) {
        polygon.getVertex(i, vertex);
        float distance = vertex.len();
        if (distance > maxDistance) {
          maxDistance = distance;
        }
      }
      radiusMeters = maxDistance;
    }

    return radiusMeters * PhysicsConstants.PIXELS_PER_METER;
  }

  private Queue<Vector2> convertGridPathToWorldPath(Queue<Vector2> gridPath) {
    Queue<Vector2> worldPath = new LinkedList<>();

    for (Vector2 gridPoint : gridPath) {
      float worldX = navigationGrid.gridToWorldX((int) gridPoint.x);
      float worldY = navigationGrid.gridToWorldY((int) gridPoint.y);
      worldPath.offer(new Vector2(worldX, worldY));
    }

    return worldPath;
  }

  private void processPathMovement(PathfindingComponent pathfinding, PositionComponent position) {
    if (!pathfinding.isPathfindingActive()) {
      return;
    }

    if (pathfinding.hasReachedCurrentWaypoint(position.x, position.y)) {
      pathfinding.advanceToNextWaypoint();
    }
  }

  private LinkedList<Vector2> getWaypointsToKeep(PathfindingComponent pathfinding) {
    LinkedList<Vector2> waypointsToKeep = new LinkedList<>();
    if (!pathfinding.hasValidPath || pathfinding.getRemainingWaypoints() < WAYPOINTS_TO_KEEP) {
      return waypointsToKeep;
    }

    LinkedList<Vector2> currentPath = new LinkedList<>(pathfinding.currentPath);
    for (int i = 0; i < WAYPOINTS_TO_KEEP && !currentPath.isEmpty(); i++) {
      waypointsToKeep.add(currentPath.poll());
    }
    return waypointsToKeep;
  }

  private int getStartGridX(PositionComponent position, Vector2 recalculateFrom) {
    return navigationGrid.worldToGridX(recalculateFrom != null ? recalculateFrom.x : position.x);
  }

  private int getStartGridY(PositionComponent position, Vector2 recalculateFrom) {
    return navigationGrid.worldToGridY(recalculateFrom != null ? recalculateFrom.y : position.y);
  }

  private LinkedList<Vector2> mergePathSegments(
      LinkedList<Vector2> waypointsToKeep, Queue<Vector2> worldPath) {
    LinkedList<Vector2> finalPath = new LinkedList<>(waypointsToKeep);
    finalPath.addAll(worldPath);
    return finalPath;
  }

  public void updateNavigationGrid(NavigationGrid newNavigationGrid) {
    if (enemyEntities != null) {
      for (int i = 0; i < enemyEntities.size(); i++) {
        Entity entity = enemyEntities.get(i);
        PathfindingComponent pathfinding = pathfindingMapper.get(entity);
        pathfinding.markForRecalculation();
      }
    }
  }
}
