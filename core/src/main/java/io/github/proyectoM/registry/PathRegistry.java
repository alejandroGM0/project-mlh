package io.github.proyectoM.registry;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/** Tracks active companion paths to avoid route overlap. */
public class PathRegistry {
  private static final float PATH_COLLISION_THRESHOLD = 50f;

  private final Map<Entity, Queue<Vector2>> activePaths = new HashMap<>();

  public void registerPath(Entity entity, Queue<Vector2> path) {
    if (entity == null || path == null) {
      return;
    }

    activePaths.put(entity, copyPath(path));
  }

  public void unregisterPath(Entity entity) {
    if (entity == null) {
      return;
    }
    activePaths.remove(entity);
  }

  public boolean hasPathCollision(Entity proposingEntity, Queue<Vector2> proposedPath) {
    if (proposedPath == null || proposedPath.isEmpty()) {
      return false;
    }

    for (Map.Entry<Entity, Queue<Vector2>> entry : activePaths.entrySet()) {
      if (entry.getKey() == proposingEntity) {
        continue;
      }
      if (pathsIntersect(proposedPath, entry.getValue())) {
        return true;
      }
    }

    return false;
  }

  public void clear() {
    activePaths.clear();
  }

  public int getActivePathCount() {
    return activePaths.size();
  }

  public void updatePath(Entity entity, Queue<Vector2> path) {
    registerPath(entity, path);
  }

  private Queue<Vector2> copyPath(Queue<Vector2> path) {
    Queue<Vector2> pathCopy = new LinkedList<>();
    for (Vector2 waypoint : path) {
      pathCopy.offer(new Vector2(waypoint));
    }
    return pathCopy;
  }

  private boolean pathsIntersect(Queue<Vector2> firstPath, Queue<Vector2> secondPath) {
    if (firstPath == null || secondPath == null) {
      return false;
    }

    for (Vector2 firstWaypoint : firstPath) {
      for (Vector2 secondWaypoint : secondPath) {
        if (firstWaypoint.dst(secondWaypoint) < PATH_COLLISION_THRESHOLD) {
          return true;
        }
      }
    }

    return false;
  }
}
