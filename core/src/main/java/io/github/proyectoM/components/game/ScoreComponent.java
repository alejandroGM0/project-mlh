package io.github.proyectoM.components.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Tracks score and end-of-run statistics for the current session. */
public class ScoreComponent implements Component, Poolable {
  public int score = 0;
  public int enemiesKilled = 0;
  public float timeSurvived = 0f;
  public int finalWave = 0;

  @Override
  public void reset() {
    score = 0;
    enemiesKilled = 0;
    timeSurvived = 0f;
    finalWave = 0;
  }
}
