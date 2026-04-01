package io.github.proyectoM;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.proyectoM.debug.DebugSystem;
import io.github.proyectoM.ecs.ProfiledEngine;
import io.github.proyectoM.localization.LocalizationManager;
import io.github.proyectoM.physics.PhysicsWorldProvider;
import io.github.proyectoM.registry.VisualAssetRegistry;
import io.github.proyectoM.resources.Assets;
import io.github.proyectoM.screens.GameSessionManager;
import io.github.proyectoM.screens.LoadingScreen;
import io.github.proyectoM.screens.ScreenManager;
import io.github.proyectoM.settings.GameSettings;

/** Owns global resources and bootstraps the initial loading flow. */
public class Main extends Game {
  public static final String TITLE = "Top-Down Shooter";
  private static final boolean CENTER_CAMERA_ON_RESIZE = true;

  private Stage gameStage;
  private Stage menuStage;
  private SpriteBatch batch;
  private ShapeRenderer shapeRenderer;

  private DebugSystem debugSystem;
  private ProfiledEngine engine;

  @Override
  public void create() {
    engine = new ProfiledEngine();
    batch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();
    gameStage = createSharedStage();
    menuStage = createSharedStage();

    initializeGlobalSystems();

    setScreen(new LoadingScreen(this));
  }

  /**
   * Both gameplay and menu stages share the same batch so rendering ownership stays centralized.
   */
  private Stage createSharedStage() {
    return new Stage(new ScreenViewport(), batch);
  }

  /**
   * Initializes singleton-style services early so loading and menu screens can assume they already
   * exist.
   */
  private void initializeGlobalSystems() {
    initializeDebugPanels();
    ScreenManager screenManager = ScreenManager.getInstance(this);
    GameSessionManager.getInstance(screenManager);
    LocalizationManager.getInstance();
    GameSettings.getInstance();
  }

  /** Debug panels are initialized once here because they reuse global UI/render resources. */
  private void initializeDebugPanels() {
    debugSystem = DebugSystem.getInstance();
  }

  public ProfiledEngine getEngine() {
    return engine;
  }

  public Stage getGameStage() {
    return gameStage;
  }

  public Stage getMenuStage() {
    return menuStage;
  }

  public SpriteBatch getBatch() {
    return batch;
  }

  public ShapeRenderer getShapeRenderer() {
    return shapeRenderer;
  }

  @Override
  /** The same viewport policy is applied to both shared stages and the debug overlay. */
  public void resize(int width, int height) {
    super.resize(width, height);
    updateStageViewport(gameStage, width, height);
    updateStageViewport(menuStage, width, height);
    if (debugSystem != null) {
      debugSystem.resize(width, height);
    }
  }

  @Override
  /**
   * Disposes top-down from screens to shared resources so no screen can outlive the global objects
   * it depends on.
   */
  public void dispose() {
    disposeCurrentScreen();
    ScreenManager.getInstance().dispose();
    GameSessionManager.resetInstance();
    if (debugSystem != null) {
      debugSystem.dispose();
    }
    disposeIfInitialized(gameStage);
    disposeIfInitialized(menuStage);
    disposeIfInitialized(shapeRenderer);
    disposeIfInitialized(batch);
    PhysicsWorldProvider.dispose();
    VisualAssetRegistry.clear();
    Assets.dispose();
    LocalizationManager.getInstance().dispose();
    GameSettings.getInstance().save();
  }

  /** Shared stages can be absent during teardown, so viewport updates stay null-safe. */
  private void updateStageViewport(Stage stage, int width, int height) {
    if (stage != null) {
      stage.getViewport().update(width, height, CENTER_CAMERA_ON_RESIZE);
    }
  }

  /** The active screen is disposed explicitly before global resources start shutting down. */
  private void disposeCurrentScreen() {
    if (getScreen() != null) {
      getScreen().dispose();
    }
  }

  /** Keeps the disposal order readable when several global resources are optional at teardown. */
  private void disposeIfInitialized(Disposable resource) {
    if (resource != null) {
      resource.dispose();
    }
  }
}
