package io.github.proyectoM.components.enemy;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores enemy-specific data that does not apply to companions. */
public class EnemyComponent implements Component, Poolable {
  public static final String DEFAULT_ENEMY_TYPE = "zombie_basic";
  public static final int DEFAULT_SCORE_POINTS = 10;

  public String enemyType = DEFAULT_ENEMY_TYPE;
  public int scorePoints = DEFAULT_SCORE_POINTS;

  public EnemyComponent() {}

  public EnemyComponent(String enemyType) {
    this.enemyType = enemyType;
  }

  @Override
  public void reset() {
    enemyType = DEFAULT_ENEMY_TYPE;
    scorePoints = DEFAULT_SCORE_POINTS;
  }
}
