package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Marker component for entities currently locked into an attack sequence. */
public class AttackingComponent implements Component, Poolable {
  @Override
  public void reset() {}
}
