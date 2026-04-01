package io.github.proyectoM.components.entity.movement;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the facing angle, in radians, for an entity. */
public class LookAtComponent implements Component, Poolable {
  public float angle = 0f;

  @Override
  public void reset() {
    angle = 0f;
  }
}
