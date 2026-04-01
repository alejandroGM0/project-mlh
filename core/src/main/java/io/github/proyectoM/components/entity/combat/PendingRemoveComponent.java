package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Marker component for entities that should be removed after the current update. */
public class PendingRemoveComponent implements Component, Poolable {
  @Override
  public void reset() {}
}
