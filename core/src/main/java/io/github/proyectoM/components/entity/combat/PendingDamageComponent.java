package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores damage that will be applied during the combat update. */
public class PendingDamageComponent implements Component, Poolable {
  public float amount;
  public Entity source;

  public PendingDamageComponent() {}

  public PendingDamageComponent(float amount) {
    this.amount = amount;
  }

  /**
   * Creates a pending damage marker with a source entity for attribution.
   *
   * @param amount damage amount to apply
   * @param source the entity that dealt the damage
   */
  public PendingDamageComponent(float amount, Entity source) {
    this.amount = amount;
    this.source = source;
  }

  @Override
  public void reset() {
    amount = 0f;
    source = null;
  }
}
