package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the entity currently being targeted. */
public class TargetComponent implements Component, Poolable {
  public Entity targetEntity;

  @Override
  public void reset() {
    targetEntity = null;
  }
}
