/**
 * WeaponStateComponent.java
 *
 * Componente ECS que almacena el estado mutable en tiempo de ejecución de un arma equipada:
 * - Estado de ataque (isAttacking, hasDamagedThisAttack).
 * - Cooldown y temporizadores de efectos visuales (cooldown, flashTimer).
 * - Referencias a entidades dinámicas (flashEntity, targetEntity).
 *
 * Se separa de WeaponComponent para distinguir los datos de plantilla (inmutables tras creación)
 * del estado que cambia frame a frame durante el combate.
 *
 * Proyecto: ProyectoM
 * Autor: AlejandroGM0
 * Fecha: 2026-04-05
 */
package io.github.proyectoM.components.entity.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores mutable runtime state for an equipped weapon, separate from its template data. */
public class WeaponStateComponent implements Component, Poolable {
  public static final float DEFAULT_COOLDOWN = 0f;
  public static final float DEFAULT_FLASH_TIMER = 0f;

  public boolean isAttacking = false;
  public float cooldown = DEFAULT_COOLDOWN;
  public float flashTimer = DEFAULT_FLASH_TIMER;
  public boolean hasDamagedThisAttack = false;
  public Entity flashEntity;
  public Entity targetEntity;

  @Override
  public void reset() {
    isAttacking = false;
    cooldown = DEFAULT_COOLDOWN;
    flashTimer = DEFAULT_FLASH_TIMER;
    hasDamagedThisAttack = false;
    flashEntity = null;
    targetEntity = null;
  }
}
