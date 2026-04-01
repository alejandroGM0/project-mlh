package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;
import io.github.proyectoM.templates.WeaponTemplate;

/** Stores mutable runtime state for an equipped weapon. */
public class WeaponComponent implements Component, Poolable {
  public static final float DEFAULT_COOLDOWN = 0f;
  public static final float DEFAULT_FLASH_TIMER = 0f;
  public static final int DEFAULT_DAMAGE_FRAME = 7;

  public String id;
  public float attackRange;
  public float targetRange;
  public float attackSpeed;
  public float reloadTime;
  public float damage;
  public String sound;
  public boolean isAttacking = false;
  public float cooldown = DEFAULT_COOLDOWN;
  public float flashTimer = DEFAULT_FLASH_TIMER;
  public int damageFrame = DEFAULT_DAMAGE_FRAME;
  public boolean hasDamagedThisAttack = false;
  public Entity flashEntity;
  public Entity targetEntity;

  public WeaponComponent() {}

  public WeaponComponent(WeaponTemplate template) {
    this.id = template.id;
    this.attackRange = template.attackRange;
    this.targetRange = template.targetRange;
    this.attackSpeed = template.attackSpeed;
    this.reloadTime = template.reloadTime;
    this.damage = template.damage;
    this.sound = template.sound;
    this.damageFrame = template.damageFrame;
  }

  @Override
  public void reset() {
    id = null;
    attackRange = 0f;
    targetRange = 0f;
    attackSpeed = 0f;
    reloadTime = 0f;
    damage = 0f;
    sound = null;
    isAttacking = false;
    cooldown = DEFAULT_COOLDOWN;
    flashTimer = DEFAULT_FLASH_TIMER;
    damageFrame = DEFAULT_DAMAGE_FRAME;
    hasDamagedThisAttack = false;
    flashEntity = null;
    targetEntity = null;
  }
}
