package io.github.proyectoM.components.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Tracks current wave progression and spawn pacing for enemy waves. */
public class WaveComponent implements Component, Poolable {
  public static final float DEFAULT_TIME_BETWEEN_WAVES = 5f;

  public int currentWave = 0;
  public float timeSinceLastWave = 0f;
  public float timeBetweenWaves = DEFAULT_TIME_BETWEEN_WAVES;
  public int enemiesToSpawn = 0;
  public boolean isWaveInProgress = false;

  @Override
  public void reset() {
    currentWave = 0;
    timeSinceLastWave = 0f;
    timeBetweenWaves = DEFAULT_TIME_BETWEEN_WAVES;
    enemiesToSpawn = 0;
    isWaveInProgress = false;
  }
}
