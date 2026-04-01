package io.github.proyectoM.components.entity.weapon.types;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Marks a weapon as ranged and stores the bullet type it fires. */
public class RangedWeaponComponent implements Component, Poolable {
  public String bulletType;

  public RangedWeaponComponent() {}

  public RangedWeaponComponent(String bulletType) {
    this.bulletType = bulletType;
  }

  @Override
  public void reset() {
    bulletType = null;
  }
}
