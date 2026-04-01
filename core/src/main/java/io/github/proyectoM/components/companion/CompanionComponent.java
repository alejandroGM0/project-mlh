package io.github.proyectoM.components.companion;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores companion-specific data that does not apply to enemies. */
public class CompanionComponent implements Component, Poolable {
  public static final String DEFAULT_COMPANION_TYPE = "soldier";
  public static final int DEFAULT_DAMAGE = 10;
  public static final float DEFAULT_RANGE_MULTIPLIER = 1f;

  public String companionType = DEFAULT_COMPANION_TYPE;
  public int damage = DEFAULT_DAMAGE;
  public float rangeMultiplier = DEFAULT_RANGE_MULTIPLIER;

  public CompanionComponent() {}

  public CompanionComponent(String companionType) {
    this.companionType = companionType;
  }

  public CompanionComponent(String companionType, int damage) {
    this.companionType = companionType;
    this.damage = damage;
  }

  @Override
  public void reset() {
    companionType = DEFAULT_COMPANION_TYPE;
    damage = DEFAULT_DAMAGE;
    rangeMultiplier = DEFAULT_RANGE_MULTIPLIER;
  }
}
