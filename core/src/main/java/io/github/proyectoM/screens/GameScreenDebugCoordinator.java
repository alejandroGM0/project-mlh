package io.github.proyectoM.screens;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.debug.AnimationPanel;
import io.github.proyectoM.debug.DebugSystem;
import io.github.proyectoM.debug.EcsMetricsPanel;
import io.github.proyectoM.debug.EntitySpawnerPanel;
import io.github.proyectoM.debug.FormationPanel;
import io.github.proyectoM.debug.PerformancePanel;
import io.github.proyectoM.debug.RenderDebugPanel;
import io.github.proyectoM.debug.RenderDebugSettings;
import io.github.proyectoM.ecs.ProfiledEngine;
import io.github.proyectoM.factories.CompanionFactory;
import io.github.proyectoM.factories.EnemyFactory;

/** Centralizes debug panel registration and frame updates for {@link GameScreen}. */
final class GameScreenDebugCoordinator {
  private final PooledEngine engine;
  private final OrthographicCamera camera;
  private final World world;
  private final CompanionFactory companionFactory;
  private final EnemyFactory enemyFactory;
  private final GameScreenMapCoordinator mapCoordinator;
  private final RenderDebugSettings renderDebugSettings;

  GameScreenDebugCoordinator(
      PooledEngine engine,
      OrthographicCamera camera,
      World world,
      CompanionFactory companionFactory,
      EnemyFactory enemyFactory,
      GameScreenMapCoordinator mapCoordinator,
      RenderDebugSettings renderDebugSettings) {
    this.engine = engine;
    this.camera = camera;
    this.world = world;
    this.companionFactory = companionFactory;
    this.enemyFactory = enemyFactory;
    this.mapCoordinator = mapCoordinator;
    this.renderDebugSettings = renderDebugSettings;
  }

  void configurePanels() {
    DebugSystem debugSystem = DebugSystem.getInstance();
    debugSystem.clearPanels();
    addCorePanels(debugSystem);
    addGameplayPanels(debugSystem);
  }

  void updateAndRender(float delta) {
    DebugSystem debugSystem = DebugSystem.getInstance();
    debugSystem.update(delta);
    debugSystem.render();
  }

  void resize(int width, int height) {
    DebugSystem.getInstance().resize(width, height);
  }

  private static final String CATEGORY_CORE = "Core";
  private static final String CATEGORY_GAMEPLAY = "Gameplay";

  private void addCorePanels(DebugSystem debugSystem) {
    debugSystem.addDebugPanel(new PerformancePanel(), CATEGORY_CORE);
    debugSystem.addDebugPanel(new EcsMetricsPanel((ProfiledEngine) engine), CATEGORY_CORE);
    debugSystem.addDebugPanel(new RenderDebugPanel(renderDebugSettings), CATEGORY_CORE);
    debugSystem.addDebugPanel(
        new EntitySpawnerPanel(engine, companionFactory, enemyFactory, world), CATEGORY_CORE);
  }

  private void addGameplayPanels(DebugSystem debugSystem) {
    debugSystem.addDebugPanel(new FormationPanel(engine), CATEGORY_GAMEPLAY);
    debugSystem.addDebugPanel(new AnimationPanel(engine), CATEGORY_GAMEPLAY);
  }
}
