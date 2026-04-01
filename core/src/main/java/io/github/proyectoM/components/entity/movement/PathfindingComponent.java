package io.github.proyectoM.components.entity.movement;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import java.util.LinkedList;
import java.util.Queue;

/** Stores the mutable state needed to request, follow, and retry paths. */
public class PathfindingComponent implements Component, Poolable {
  public static final float DEFAULT_RECALCULATE_INTERVAL = 2f;
  public static final float DEFAULT_RECALCULATE_COOLDOWN = 0.5f;
  public static final float DEFAULT_FAILED_PATH_BACKOFF = 5f;
  public static final float DEFAULT_TARGET_MOVEMENT_THRESHOLD = 150f;
  public static final float DEFAULT_GOAL_TOLERANCE = 80f;
  public static final float DEFAULT_WAYPOINT_REACHED_DISTANCE = 10f;

  public final Queue<Vector2> currentPath = new LinkedList<>();
  public final Vector2 targetPosition = new Vector2();
  public final Vector2 lastCalculatedTarget = new Vector2();
  public final Vector2 currentWaypoint = new Vector2();

  public boolean needsNewPath = false;
  public boolean hasValidPath = false;
  public boolean pathfindingEnabled = true;
  public float recalculateTimer = 0f;
  public float recalculateInterval = DEFAULT_RECALCULATE_INTERVAL;
  public float recalculateCooldown = DEFAULT_RECALCULATE_COOLDOWN;
  public float cooldownTimer = 0f;
  public float failedPathBackoff = DEFAULT_FAILED_PATH_BACKOFF;
  public float failedPathTimer = 0f;
  public boolean lastPathCalculationFailed = false;
  public float targetMovementThreshold = DEFAULT_TARGET_MOVEMENT_THRESHOLD;
  public float goalTolerance = DEFAULT_GOAL_TOLERANCE;
  public float waypointReachedDistance = DEFAULT_WAYPOINT_REACHED_DISTANCE;
  public boolean reachedDestination = false;

  public PathfindingComponent() {}

  public PathfindingComponent(
      float recalculateInterval, float targetMovementThreshold, float waypointReachedDistance) {
    this.recalculateInterval = recalculateInterval;
    this.targetMovementThreshold = targetMovementThreshold;
    this.waypointReachedDistance = waypointReachedDistance;
  }

  public void setTarget(float targetX, float targetY) {
    float previousX = targetPosition.x;
    float previousY = targetPosition.y;
    targetPosition.set(targetX, targetY);

    float distanceMoved = Vector2.dst(previousX, previousY, targetX, targetY);
    if (distanceMoved >= targetMovementThreshold) {
      markForRecalculation();
    }
  }

  public void setTarget(Vector2 target) {
    setTarget(target.x, target.y);
  }

  public void markForRecalculation() {
    needsNewPath = true;
    hasValidPath = false;
    reachedDestination = false;
    recalculateTimer = 0f;
  }

  public void setPath(Queue<Vector2> newPath) {
    currentPath.clear();
    if (newPath == null || newPath.isEmpty()) {
      clearCurrentWaypoint();
      hasValidPath = false;
      reachedDestination = true;
    } else {
      currentPath.addAll(newPath);
      hasValidPath = true;
      reachedDestination = false;
      updateCurrentWaypoint();
    }

    lastCalculatedTarget.set(targetPosition);
    needsNewPath = false;
  }

  public void updateCurrentWaypoint() {
    if (currentPath.isEmpty()) {
      clearCurrentWaypoint();
      return;
    }

    Vector2 nextWaypoint = currentPath.peek();
    if (nextWaypoint != null) {
      currentWaypoint.set(nextWaypoint);
    }
  }

  public boolean advanceToNextWaypoint() {
    if (currentPath.isEmpty()) {
      return false;
    }

    currentPath.poll();
    if (!currentPath.isEmpty()) {
      updateCurrentWaypoint();
      return true;
    }

    clearCurrentWaypoint();
    reachedDestination = true;
    hasValidPath = false;
    return false;
  }

  public boolean hasReachedCurrentWaypoint(float entityX, float entityY) {
    if (!hasValidPath || currentPath.isEmpty()) {
      return false;
    }

    float distance = Vector2.dst(entityX, entityY, currentWaypoint.x, currentWaypoint.y);
    return distance <= waypointReachedDistance;
  }

  public void clearPath() {
    currentPath.clear();
    clearCurrentWaypoint();
    hasValidPath = false;
    needsNewPath = false;
    reachedDestination = false;
    recalculateTimer = 0f;
    lastPathCalculationFailed = true;
    failedPathTimer = 0f;
  }

  public void markPathCalculationSuccess() {
    lastPathCalculationFailed = false;
    failedPathTimer = 0f;
  }

  public boolean updateFailedPathBackoff(float deltaTime) {
    if (!lastPathCalculationFailed) {
      return true;
    }

    failedPathTimer += deltaTime;
    return failedPathTimer >= failedPathBackoff;
  }

  public boolean updateCooldownTimer(float deltaTime) {
    cooldownTimer += deltaTime;
    return cooldownTimer >= recalculateCooldown;
  }

  public void resetCooldown() {
    cooldownTimer = 0f;
  }

  public boolean isWithinGoalTolerance(float entityX, float entityY) {
    float distance = Vector2.dst(entityX, entityY, targetPosition.x, targetPosition.y);
    return distance <= goalTolerance;
  }

  public boolean updateRecalculateTimer(float deltaTime) {
    if (!pathfindingEnabled) {
      return false;
    }

    recalculateTimer += deltaTime;
    if (recalculateTimer < recalculateInterval) {
      return false;
    }

    recalculateTimer = 0f;
    return true;
  }

  public boolean shouldRecalculateForTargetMovement() {
    if (!pathfindingEnabled || !hasValidPath) {
      return false;
    }

    float distanceMoved =
        Vector2.dst(
            lastCalculatedTarget.x, lastCalculatedTarget.y, targetPosition.x, targetPosition.y);
    return distanceMoved >= targetMovementThreshold;
  }

  public int getRemainingWaypoints() {
    return currentPath.size();
  }

  public boolean isPathfindingActive() {
    return pathfindingEnabled && hasValidPath && !currentPath.isEmpty();
  }

  @Override
  public void reset() {
    currentPath.clear();
    targetPosition.setZero();
    lastCalculatedTarget.setZero();
    currentWaypoint.setZero();
    needsNewPath = false;
    hasValidPath = false;
    pathfindingEnabled = true;
    recalculateTimer = 0f;
    recalculateInterval = DEFAULT_RECALCULATE_INTERVAL;
    recalculateCooldown = DEFAULT_RECALCULATE_COOLDOWN;
    cooldownTimer = 0f;
    failedPathBackoff = DEFAULT_FAILED_PATH_BACKOFF;
    failedPathTimer = 0f;
    lastPathCalculationFailed = false;
    targetMovementThreshold = DEFAULT_TARGET_MOVEMENT_THRESHOLD;
    goalTolerance = DEFAULT_GOAL_TOLERANCE;
    waypointReachedDistance = DEFAULT_WAYPOINT_REACHED_DISTANCE;
    reachedDestination = false;
  }

  private void clearCurrentWaypoint() {
    currentWaypoint.setZero();
  }
}
