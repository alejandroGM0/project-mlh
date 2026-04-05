package io.github.proyectoM.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.proyectoM.components.entity.combat.HealthComponent;
import org.junit.jupiter.api.Test;

class HealthComponentTest {

  private static final int DEFAULT_HEALTH = 100;

  /**
   * Verifies that the default constructor initializes with 100 health.
   */
  @Test
  void defaultConstructor_setsHealthTo100() {
    HealthComponent health = new HealthComponent();

    assertEquals(DEFAULT_HEALTH, health.maxHealth);
    assertEquals(DEFAULT_HEALTH, health.currentHealth);
  }

  /**
   * Verifies that the single-parameter constructor assigns the value to both fields.
   */
  @Test
  void singleParamConstructor_setsBothFields() {
    HealthComponent health = new HealthComponent(250);

    assertEquals(250, health.maxHealth);
    assertEquals(250, health.currentHealth);
  }

  /**
   * Verifies that the two-parameter constructor assigns different values.
   */
  @Test
  void twoParamConstructor_setsDifferentValues() {
    HealthComponent health = new HealthComponent(300, 150);

    assertEquals(300, health.maxHealth);
    assertEquals(150, health.currentHealth);
  }

  /**
   * Verifies that reset() restores both fields to 100.
   */
  @Test
  void reset_restoresDefaults() {
    HealthComponent health = new HealthComponent(500, 200);

    health.reset();

    assertEquals(DEFAULT_HEALTH, health.maxHealth);
    assertEquals(DEFAULT_HEALTH, health.currentHealth);
  }

  /**
   * Verifies that current health can be zero (dead entity).
   */
  @Test
  void zeroCurrentHealth_representsDeadEntity() {
    HealthComponent health = new HealthComponent(100, 0);

    assertEquals(100, health.maxHealth);
    assertEquals(0, health.currentHealth);
  }

  /**
   * Verifies that current health can be modified directly.
   */
  @Test
  void currentHealth_canBeModified() {
    HealthComponent health = new HealthComponent(100);

    health.currentHealth -= 30;

    assertEquals(100, health.maxHealth);
    assertEquals(70, health.currentHealth);
  }
}