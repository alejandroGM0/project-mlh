package io.github.proyectoM.components.entity.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores an entity's current and maximum health. */
public class HealthComponent implements Component, Poolable {
  public int maxHealth = 100;
  public int currentHealth = 100;

  public HealthComponent() {}

  /**
   * Creates a component with the same current and maximum health.
   *
   * @param health health value assigned to both fields
   */
  public HealthComponent(int health) {
    this.maxHealth = health;
    this.currentHealth = health;
  }

  /**
   * Creates a component with explicit maximum and current health values.
   *
   * @param maxHealth maximum health value
   * @param currentHealth current health value
   */
  public HealthComponent(int maxHealth, int currentHealth) {
    this.maxHealth = maxHealth;
    this.currentHealth = currentHealth;
  }

  @Override
  public void reset() {
    maxHealth = 100;
    currentHealth = 100;
  }
}
