package io.github.proyectoM.components.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Links a child entity to the parent entity that owns or drives it. */
public class ParentComponent implements Component, Poolable {
  public Entity parent;

  public ParentComponent() {}

  public ParentComponent(Entity parent) {
    this.parent = parent;
  }

  @Override
  public void reset() {
    parent = null;
  }
}
