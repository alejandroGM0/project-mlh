package io.github.proyectoM.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.proyectoM.Main;
import io.github.proyectoM.components.game.ScoreComponent;
import io.github.proyectoM.physics.PhysicsWorldProvider;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Coordinates screen creation, caching, disposal, and transitions. */
public class ScreenManager {
  public enum ScreenType {
    MAIN_MENU,
    GAME_SETUP,
    GAME,
    PAUSE,
    SETTINGS,
    GAME_OVER,
    OPTIONS
  }

  private static ScreenManager instance;

  private final Main game;
  private final Map<ScreenType, Screen> screenCache;

  private ScreenManager(Main game) {
    this.game = Objects.requireNonNull(game, "game");
    this.screenCache = new EnumMap<>(ScreenType.class);
  }

  public static ScreenManager getInstance(Main game) {
    if (instance == null) {
      instance = new ScreenManager(game);
    }
    return instance;
  }

  public static ScreenManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException(
          "ScreenManager not initialized. Call getInstance(Main game) first.");
    }
    return instance;
  }

  /**
   * Reuses cached screens when possible, but clears the shared menu stage first for non-game
   * screens so stale actors do not leak across transitions.
   */
  public void showScreen(ScreenType type) {
    Screen screen = getOrCreateScreen(type);
    if (usesMenuStage(type)) {
      clearMenuStage();
    }
    game.setScreen(screen);
  }

  /** Shows the options screen, returning to {@code returnTo} when Back is pressed. */
  public void showOptionsFrom(ScreenType returnTo) {
    disposeCachedScreen(ScreenType.OPTIONS);
    clearMenuStage();
    OptionsScreen screen = new OptionsScreen(game, returnTo);
    screenCache.put(ScreenType.OPTIONS, screen);
    game.setScreen(screen);
  }

  /** Removes cached menu screens so they are recreated with fresh translations. */
  public void clearMenuScreenCaches() {
    disposeCachedScreen(ScreenType.MAIN_MENU);
    disposeCachedScreen(ScreenType.GAME_SETUP);
    disposeCachedScreen(ScreenType.PAUSE);
    disposeCachedScreen(ScreenType.SETTINGS);
    disposeCachedScreen(ScreenType.OPTIONS);
    disposeCachedScreen(ScreenType.GAME_OVER);
  }

  public void dispose() {
    for (Screen screen : screenCache.values()) {
      screen.dispose();
    }
    screenCache.clear();
    instance = null;
  }

  /**
   * Creates a fresh gameplay screen after a session reset.
   *
   * <p>Both shared stages are cleared first because `GameScreen` rebuilds their actors as part of
   * its own initialization.
   */
  void showNewGameScreen() {
    showNewGameScreen(null, null);
  }

  /**
   * Creates a fresh gameplay screen after a session reset, loading the specified map.
   *
   * @param mapId the registry identifier of the map to load, or null for default
   * @param gameMode the selected game mode, or null for default
   */
  void showNewGameScreen(String mapId, GameMode gameMode) {
    disposeCachedScreen(ScreenType.GAME);
    PhysicsWorldProvider.resetWorld();
    clearMenuStage();
    clearGameStage();

    GameScreen gameScreen = new GameScreen(game, mapId, gameMode);
    screenCache.put(ScreenType.GAME, gameScreen);
    game.setScreen(gameScreen);
  }

  /** Creates a GameOverScreen with final stats. Called by GameSessionManager. */
  void showGameOverScreen(ScoreComponent finalStats) {
    disposeCachedScreen(ScreenType.GAME_OVER);
    clearMenuStage();
    GameOverScreen screen = new GameOverScreen(game, finalStats);
    screenCache.put(ScreenType.GAME_OVER, screen);
    game.setScreen(screen);
  }

  /** Cached screens are created lazily so menu/localization state can be refreshed on demand. */
  private Screen getOrCreateScreen(ScreenType type) {
    Screen screen = screenCache.get(type);
    if (screen != null) {
      return screen;
    }

    Screen createdScreen = createScreen(type);
    screenCache.put(type, createdScreen);
    return createdScreen;
  }

  /** `GAME_OVER` is excluded here because it requires run-specific score data. */
  private Screen createScreen(ScreenType type) {
    switch (type) {
      case MAIN_MENU:
        return new MainMenuScreen(game);
      case GAME_SETUP:
        return new GameSetupScreen(game);
      case GAME:
        return new GameScreen(game, null, null);
      case PAUSE:
        return new PauseMenuScreen(game);
      case SETTINGS:
        return new SettingsScreen(game);
      case OPTIONS:
        return new OptionsScreen(game);
      case GAME_OVER:
        throw new UnsupportedOperationException(
            "GAME_OVER requires score data."
                + " Use GameSessionManager.getInstance().showGameOver() instead.");
      default:
        throw new IllegalArgumentException("Unknown screen type: " + type);
    }
  }

  private void disposeCachedScreen(ScreenType type) {
    Screen screen = screenCache.remove(type);
    if (screen != null) {
      screen.dispose();
    }
  }

  private boolean usesMenuStage(ScreenType type) {
    return type != ScreenType.GAME;
  }

  /** Menu screens share a single `Stage`, so transitions always start from a clean actor tree. */
  private void clearMenuStage() {
    Stage menuStage = game.getMenuStage();
    if (menuStage != null) {
      menuStage.clear();
    }
  }

  /** Gameplay screens also share one `Stage`, so a new run must clear old actors explicitly. */
  private void clearGameStage() {
    Stage gameStage = game.getGameStage();
    if (gameStage != null) {
      gameStage.clear();
    }
  }
}
