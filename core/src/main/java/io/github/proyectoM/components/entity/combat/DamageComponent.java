package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the damage an entity can inflict. */
public class DamageComponent implements Component, Poolable {
  public static final int DEFAULT_DAMAGE = 10;

  public int damage = DEFAULT_DAMAGE;

  public DamageComponent() {}

  public DamageComponent(int damage) {
    this.damage = damage;
  }

  @Override
  public void reset() {
    damage = DEFAULT_DAMAGE;
  }
}
