package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores a request to shoot toward a specific world-space target. */
public class ShootingIntentComponent implements Component, Poolable {
  public boolean shoot = false;
  public float targetX = 0f;
  public float targetY = 0f;

  @Override
  public void reset() {
    shoot = false;
    targetX = 0f;
    targetY = 0f;
  }
}
