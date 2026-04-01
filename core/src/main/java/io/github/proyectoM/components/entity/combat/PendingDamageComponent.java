package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores damage that will be applied during the combat update. */
public class PendingDamageComponent implements Component, Poolable {
  public float amount;

  public PendingDamageComponent() {}

  public PendingDamageComponent(float amount) {
    this.amount = amount;
  }

  @Override
  public void reset() {
    amount = 0f;
  }
}
