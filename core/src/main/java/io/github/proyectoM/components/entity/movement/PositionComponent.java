package io.github.proyectoM.components.entity.movement;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores an entity position in world-space pixels. */
public class PositionComponent implements Component, Poolable {
  public float x = 0f;
  public float y = 0f;

  public PositionComponent() {}

  public PositionComponent(float x, float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void reset() {
    x = 0f;
    y = 0f;
  }
}
