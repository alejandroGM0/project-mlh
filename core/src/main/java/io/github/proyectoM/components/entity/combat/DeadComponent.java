package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Tracks corpse lifetime and animation completion after an entity dies. */
public class DeadComponent implements Component, Poolable {
  public float timeSinceDeath = 0f;
  public boolean animationFinished = false;

  @Override
  public void reset() {
    timeSinceDeath = 0f;
    animationFinished = false;
  }
}
