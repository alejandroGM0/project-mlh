package io.github.proyectoM.screens;

import io.github.proyectoM.components.game.ScoreComponent;

/** Owns game lifecycle transitions: starting new runs and ending them on game-over. */
public class GameSessionManager {
  private static GameSessionManager instance;

  private final ScreenManager screenManager;

  private GameSessionManager(ScreenManager screenManager) {
    this.screenManager = screenManager;
  }

  /** Initializes the singleton. Must be called once with a valid ScreenManager. */
  public static GameSessionManager getInstance(ScreenManager screenManager) {
    if (instance == null) {
      instance = new GameSessionManager(screenManager);
    }
    return instance;
  }

  /** Returns the existing singleton. */
  public static GameSessionManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException(
          "GameSessionManager not initialized. Call getInstance(ScreenManager) first.");
    }
    return instance;
  }

  /** Clears the singleton so a fresh instance is created on the next access. */
  public static void resetInstance() {
    instance = null;
  }

  /** Starts a new game session using the default map. */
  public void startNewGame() {
    screenManager.showNewGameScreen();
  }

  /**
   * Starts a new game session with a specific map and game mode.
   *
   * @param mapId the registry identifier of the map to load
   * @param gameMode the selected game mode
   */
  public void startNewGame(String mapId, GameMode gameMode) {
    screenManager.showNewGameScreen(mapId, gameMode);
  }

  /** Transitions to the game-over screen with the final run statistics. */
  public void showGameOver(ScoreComponent finalStats) {
    screenManager.showGameOverScreen(finalStats);
  }
}
