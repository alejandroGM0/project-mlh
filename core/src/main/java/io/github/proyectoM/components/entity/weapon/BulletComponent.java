package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores runtime state for bullets, including distance, homing, and visual rotation. */
public class BulletComponent implements Component, Poolable {
  public static final float DEFAULT_DISTANCE_TRAVELLED = 0f;
  public static final float DEFAULT_START_POSITION = 0f;
  public static final float DEFAULT_TARGET_ROTATION = 0f;
  public static final float DEFAULT_ROTATION_SPEED = 720f;
  public static final float DEFAULT_TARGET_POSITION = 0f;
  public static final float DEFAULT_HOMING_STRENGTH = 3f;
  public static final float DEFAULT_SPEED = 0f;

  public float distanceTravelled = DEFAULT_DISTANCE_TRAVELLED;
  public float maxDistance = Float.POSITIVE_INFINITY;
  public float startX = DEFAULT_START_POSITION;
  public float startY = DEFAULT_START_POSITION;
  public float targetRotation = DEFAULT_TARGET_ROTATION;
  public boolean isRotating = false;
  public float rotationSpeed = DEFAULT_ROTATION_SPEED;
  public boolean isHoming = false;
  public float targetX = DEFAULT_TARGET_POSITION;
  public float targetY = DEFAULT_TARGET_POSITION;
  public float homingStrength = DEFAULT_HOMING_STRENGTH;
  public float speed = DEFAULT_SPEED;

  public BulletComponent() {}

  public BulletComponent(float maxDistance) {
    this.maxDistance = maxDistance;
  }

  public BulletComponent(float maxDistance, float startX, float startY) {
    this.maxDistance = maxDistance;
    this.startX = startX;
    this.startY = startY;
  }

  @Override
  public void reset() {
    distanceTravelled = DEFAULT_DISTANCE_TRAVELLED;
    maxDistance = Float.POSITIVE_INFINITY;
    startX = DEFAULT_START_POSITION;
    startY = DEFAULT_START_POSITION;
    targetRotation = DEFAULT_TARGET_ROTATION;
    isRotating = false;
    rotationSpeed = DEFAULT_ROTATION_SPEED;
    isHoming = false;
    targetX = DEFAULT_TARGET_POSITION;
    targetY = DEFAULT_TARGET_POSITION;
    homingStrength = DEFAULT_HOMING_STRENGTH;
    speed = DEFAULT_SPEED;
  }
}
