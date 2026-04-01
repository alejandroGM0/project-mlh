package io.github.proyectoM.components.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** A unified ECS component for AI entity statistics. Used by both companions and enemies. */
public class AIComponent implements Component, Poolable {
  public static final String DEFAULT_ID = "";
  public static final String DEFAULT_NAME = "";
  public static final float DEFAULT_CURRENT_COOLDOWN_SECONDS = 0f;

  private static final float MILLISECONDS_PER_SECOND = 1000f;

  public String id = DEFAULT_ID;
  public String name = DEFAULT_NAME;

  public float speed;
  public float armor;
  public float baseDamage;
  public float baseRange;
  public float mass;

  public float cooldownAttack;
  public float currentCooldown = DEFAULT_CURRENT_COOLDOWN_SECONDS;

  public float spawnTime;

  public AIComponent() {
    // Required for Ashley component creation.
  }

  /**
   * Constructor with main parameters.
   *
   * @param id Unique ID of the entity.
   * @param name Name of the entity.
   * @param speed Movement speed.
   * @param armor Base armor.
   * @param baseDamage Base attack damage.
   * @param baseRange Detection/attack range.
   * @param cooldownAttack Cooldown between attacks.
   * @param mass Physical mass of the entity.
   */
  public AIComponent(
      String id,
      String name,
      float speed,
      float armor,
      float baseDamage,
      float baseRange,
      float cooldownAttack,
      float mass) {
    this.id = id;
    this.name = name;
    this.speed = speed;
    this.armor = armor;
    this.baseDamage = baseDamage;
    this.baseRange = baseRange;
    this.cooldownAttack = cooldownAttack;
    this.mass = mass;
    this.spawnTime = getCurrentTimeSeconds();
  }

  @Override
  public void reset() {
    id = DEFAULT_ID;
    name = DEFAULT_NAME;
    speed = 0f;
    armor = 0f;
    baseDamage = 0f;
    baseRange = 0f;
    mass = 0f;
    cooldownAttack = 0f;
    currentCooldown = DEFAULT_CURRENT_COOLDOWN_SECONDS;
    spawnTime = 0f;
  }

  private static float getCurrentTimeSeconds() {
    return System.currentTimeMillis() / MILLISECONDS_PER_SECOND;
  }
}
