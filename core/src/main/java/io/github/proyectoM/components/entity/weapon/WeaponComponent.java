package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores immutable template data for an equipped weapon. */
public class WeaponComponent implements Component, Poolable {
  public static final int DEFAULT_DAMAGE_FRAME = 7;

  public String id;
  public float attackRange;
  public float targetRange;
  public float attackSpeed;
  public float reloadTime;
  public float damage;
  public String sound;
  public int damageFrame = DEFAULT_DAMAGE_FRAME;

  @Override
  public void reset() {
    id = null;
    attackRange = 0f;
    targetRange = 0f;
    attackSpeed = 0f;
    reloadTime = 0f;
    damage = 0f;
    sound = null;
    damageFrame = DEFAULT_DAMAGE_FRAME;
  }
}
