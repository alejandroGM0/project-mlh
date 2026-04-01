package io.github.proyectoM.components.entity.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores derived movement direction data used by animation selection and movement systems. */
public class MovementDirectionStateComponent implements Component, Poolable {
  public static final float ANGLE_THRESHOLD = 0.1f;
  public static final float VELOCITY_THRESHOLD = 0.05f;

  public MovementType movementType = MovementType.FORWARD;
  public int directionIndex = 0;
  public float faceAngle = 0f;
  public boolean isMoving = false;
  public float cachedFaceAngle = 0f;
  public float cachedVelocityMagnitude = 0f;
  public float cachedVelocityAngle = 0f;

  @Override
  public void reset() {
    movementType = MovementType.FORWARD;
    directionIndex = 0;
    faceAngle = 0f;
    isMoving = false;
    cachedFaceAngle = 0f;
    cachedVelocityMagnitude = 0f;
    cachedVelocityAngle = 0f;
  }

  /** Classifies movement relative to the entity's facing direction. */
  public enum MovementType {
    FORWARD,
    BACKWARDS,
    STRAFE_LEFT,
    STRAFE_RIGHT
  }
}
