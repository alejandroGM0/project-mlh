package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Legacy bullet distance component kept for compatibility with older entity setups. */
public class BulletDistanceComponent implements Component, Poolable {
  public static final float DEFAULT_DISTANCE_TRAVELLED = 0f;
  public static final float DEFAULT_START_POSITION = 0f;

  public float distanceTravelled = DEFAULT_DISTANCE_TRAVELLED;
  public float maxDistance;
  public float startX = DEFAULT_START_POSITION;
  public float startY = DEFAULT_START_POSITION;

  public BulletDistanceComponent() {}

  public BulletDistanceComponent(float maxDistance) {
    this.maxDistance = maxDistance;
  }

  public BulletDistanceComponent(float maxDistance, float startX, float startY) {
    this.maxDistance = maxDistance;
    this.startX = startX;
    this.startY = startY;
  }

  @Override
  public void reset() {
    distanceTravelled = DEFAULT_DISTANCE_TRAVELLED;
    maxDistance = 0f;
    startX = DEFAULT_START_POSITION;
    startY = DEFAULT_START_POSITION;
  }
}
