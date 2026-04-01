package io.github.proyectoM.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.ScoreComponent;
import io.github.proyectoM.components.game.WaveComponent;

/** Owns global gameplay state setup and screen transitions for {@link GameScreen}. */
final class GameScreenStateCoordinator {
  private final PooledEngine engine;

  private Entity globalStateEntity;
  private boolean gameOverTriggered;

  GameScreenStateCoordinator(PooledEngine engine) {
    this.engine = engine;
  }

  void initializeGlobalStateEntity() {
    gameOverTriggered = false;
    globalStateEntity = engine.createEntity();

    GameStateComponent gameState = engine.createComponent(GameStateComponent.class);
    gameState.currentState = GameStateComponent.State.RUNNING;
    globalStateEntity.add(gameState);

    WaveComponent wave = engine.createComponent(WaveComponent.class);
    wave.currentWave = 1;
    globalStateEntity.add(wave);

    ScoreComponent score = engine.createComponent(ScoreComponent.class);
    globalStateEntity.add(score);

    engine.addEntity(globalStateEntity);
  }

  void showPauseScreen() {
    ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.PAUSE);
  }

  /**
   * @return true si se ha realizado la transición a Game Over y no se debe seguir renderizando.
   */
  boolean updateGameOverTransition() {
    if (gameOverTriggered || globalStateEntity == null) {
      return gameOverTriggered;
    }

    GameStateComponent gameState = globalStateEntity.getComponent(GameStateComponent.class);
    if (gameState == null || gameState.currentState != GameStateComponent.State.GAME_OVER) {
      return false;
    }

    gameOverTriggered = true;
    ScoreComponent scoreComponent = globalStateEntity.getComponent(ScoreComponent.class);
    WaveComponent waveComponent = globalStateEntity.getComponent(WaveComponent.class);
    if (scoreComponent != null && waveComponent != null) {
      scoreComponent.finalWave = waveComponent.currentWave;
    }

    GameSessionManager.getInstance().showGameOver(scoreComponent);
    return true;
  }
}
