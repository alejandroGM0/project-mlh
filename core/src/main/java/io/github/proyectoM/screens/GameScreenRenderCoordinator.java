package io.github.proyectoM.screens;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.debug.RenderDebugSettings;
import io.github.proyectoM.physics.PhysicsConstants;

/** Runs the frame rendering pipeline for {@link GameScreen}. */
final class GameScreenRenderCoordinator {
  private static final float FULL_ALPHA = 1f;
  private static final float CLEAR_COLOR_RED = 0.15f;
  private static final float CLEAR_COLOR_GREEN = 0.15f;
  private static final float CLEAR_COLOR_BLUE = 0.15f;
  private static final float LEADER_MARKER_RADIUS = 12f;

  private final SpriteBatch batch;
  private final Stage uiStage;
  private final OrthographicCamera camera;
  private final PooledEngine engine;
  private final World world;
  private final ShapeRenderer shapeRenderer;
  private final Box2DDebugRenderer debugRenderer;
  private final RayHandler rayHandler;
  private final RenderDebugSettings renderDebugSettings;
  private final GameScreenMapCoordinator mapCoordinator;
  private final GameScreenDebugCoordinator debugCoordinator;
  private final Matrix4 lightingMatrix = new Matrix4();

  GameScreenRenderCoordinator(
      SpriteBatch batch,
      Stage uiStage,
      OrthographicCamera camera,
      PooledEngine engine,
      World world,
      ShapeRenderer shapeRenderer,
      Box2DDebugRenderer debugRenderer,
      RayHandler rayHandler,
      RenderDebugSettings renderDebugSettings,
      GameScreenMapCoordinator mapCoordinator,
      GameScreenDebugCoordinator debugCoordinator) {
    this.batch = batch;
    this.uiStage = uiStage;
    this.camera = camera;
    this.engine = engine;
    this.world = world;
    this.shapeRenderer = shapeRenderer;
    this.debugRenderer = debugRenderer;
    this.rayHandler = rayHandler;
    this.renderDebugSettings = renderDebugSettings;
    this.mapCoordinator = mapCoordinator;
    this.debugCoordinator = debugCoordinator;
  }

  void renderFrame(float delta) {
    clearScreen();
    mapCoordinator.render(camera);
    renderEntities(delta);
    renderLighting();
    renderUiOverlay();
    renderDebugPhysics();
    renderLeaderMarkerOverlay();
    debugCoordinator.updateAndRender(delta);
  }

  private void clearScreen() {
    Gdx.gl.glClearColor(CLEAR_COLOR_RED, CLEAR_COLOR_GREEN, CLEAR_COLOR_BLUE, FULL_ALPHA);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  private void renderEntities(float delta) {
    batch.setProjectionMatrix(camera.combined);
    engine.update(delta);
  }

  private void renderLighting() {
    if (rayHandler == null || !renderDebugSettings.lightingEnabled) {
      return;
    }

    lightingMatrix.set(camera.combined);
    lightingMatrix.scl(PhysicsConstants.PIXELS_PER_METER);
    rayHandler.setCombinedMatrix(lightingMatrix);
    rayHandler.updateAndRender();
  }

  private void renderUiOverlay() {
    if (uiStage == null) {
      return;
    }

    uiStage.getViewport().apply();
    uiStage.draw();
  }

  private void renderDebugPhysics() {
    if (!renderDebugSettings.physicsEnabled || debugRenderer == null) {
      return;
    }

    debugRenderer.render(world, camera.combined.cpy().scl(PhysicsConstants.PIXELS_PER_METER));
  }

  private void renderLeaderMarkerOverlay() {
    if (!renderDebugSettings.leaderMarkerEnabled) {
      return;
    }

    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (Entity entity :
        engine.getEntitiesFor(
            Family.all(GroupControllerComponent.class, PositionComponent.class).get())) {
      PositionComponent position = entity.getComponent(PositionComponent.class);
      if (position != null) {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(position.x, position.y, LEADER_MARKER_RADIUS);
        break;
      }
    }

    shapeRenderer.end();
  }
}
