package io.github.proyectoM.systems.enemy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.WaveComponent;

/** A system that controls the progression of waves based on elapsed time. */
public class WaveSystem extends EntitySystem {
  private static final float BASE_SPAWN_INTERVAL = 2f;
  private static final float DIFFICULTY_MULTIPLIER_FACTOR = 0.1f;
  private static final int ZOMBIES_PER_SPAWN_STEP = 2;
  private static final int BASE_ZOMBIES_PER_SPAWN = 1;

  private final ComponentMapper<WaveComponent> waveMapper =
      ComponentMapper.getFor(WaveComponent.class);
  private final ComponentMapper<GameStateComponent> gameStateMapper =
      ComponentMapper.getFor(GameStateComponent.class);

  private ImmutableArray<Entity> stateEntities;

  /** Constructor for the WaveSystem. */
  public WaveSystem() {
    super();
  }

  @Override
  public void addedToEngine(Engine engine) {
    stateEntities =
        engine.getEntitiesFor(Family.all(GameStateComponent.class, WaveComponent.class).get());
  }

  /**
   * Updates the time and calculates the current wave. It only advances if the game is in the
   * RUNNING state.
   *
   * @param deltaTime The time elapsed since the last frame.
   */
  @Override
  public void update(float deltaTime) {
    if (stateEntities.size() == 0) {
      return;
    }

    Entity globalStateEntity = stateEntities.first();
    GameStateComponent gameState = gameStateMapper.get(globalStateEntity);
    if (gameState.currentState != GameStateComponent.State.RUNNING) {
      return;
    }

    WaveComponent wave = waveMapper.get(globalStateEntity);
    wave.timeSinceLastWave += deltaTime;

    if (wave.timeSinceLastWave >= wave.timeBetweenWaves) {
      wave.currentWave++;
      wave.timeSinceLastWave = 0f;
      recalculateWaveParameters(wave);
    }
  }

  /**
   * Recalculates the spawn interval, difficulty multiplier, and zombies per spawn for the given
   * wave. Called once each time a new wave starts.
   *
   * @param wave the wave component to update
   */
  private void recalculateWaveParameters(WaveComponent wave) {
    wave.difficultyMultiplier = (1.0f + wave.currentWave) * DIFFICULTY_MULTIPLIER_FACTOR;
    wave.spawnInterval = BASE_SPAWN_INTERVAL / wave.difficultyMultiplier;
    wave.zombiesPerSpawn = BASE_ZOMBIES_PER_SPAWN + (wave.currentWave - 1) / ZOMBIES_PER_SPAWN_STEP;
  }
}
