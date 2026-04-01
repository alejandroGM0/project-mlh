package io.github.proyectoM.systems.enemy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.WaveComponent;

/** A system that controls the progression of waves based on elapsed time. */
public class WaveSystem extends EntitySystem {

  private final ComponentMapper<WaveComponent> waveMapper =
      ComponentMapper.getFor(WaveComponent.class);
  private final ComponentMapper<GameStateComponent> gameStateMapper =
      ComponentMapper.getFor(GameStateComponent.class);

  /** Constructor for the WaveSystem. */
  public WaveSystem() {
    super();
  }

  /**
   * Updates the time and calculates the current wave. It only advances if the game is in the
   * RUNNING state.
   *
   * @param deltaTime The time elapsed since the last frame.
   */
  @Override
  public void update(float deltaTime) {
    ImmutableArray<Entity> stateEntities =
        getEngine().getEntitiesFor(Family.all(GameStateComponent.class, WaveComponent.class).get());
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
    }
  }
}
