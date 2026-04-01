package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the world-space muzzle position used by firing and effects systems. */
public class MuzzlePointComponent implements Component, Poolable {
  public final Vector2 position = new Vector2();

  @Override
  public void reset() {
    position.setZero();
  }
}
