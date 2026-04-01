package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;

/** Renders debug information for companion formation layout. */
public class FormationVisualizationSystem extends EntitySystem {
  private static final float CENTER_CIRCLE_RADIUS = 8f;
  private static final float COMPANION_CIRCLE_RADIUS = 6f;
  private static final float FORMATION_TEXT_X_OFFSET = 100f;
  private static final float FORMATION_TEXT_Y_OFFSET = 50f;
  private static final float FORMATION_STATE_X_OFFSET = 50f;
  private static final float FORMATION_STATE_Y_OFFSET = 30f;
  private static final float LINE_RED = 1f;
  private static final float LINE_GREEN = 1f;
  private static final float LINE_BLUE = 0f;
  private static final float LINE_ALPHA = 0.5f;
  private static final String ACTIVE_STATE_TEXT = "ACTIVE";
  private static final Color CENTER_COLOR = Color.YELLOW;
  private static final Color LINE_COLOR = new Color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA);
  private static final Color COMPANION_COLOR = Color.CYAN;
  private static final Color FORMATION_TEXT_COLOR = Color.WHITE;

  private final ShapeRenderer shapeRenderer;
  private final SpriteBatch batch;
  private final BitmapFont font;
  private final OrthographicCamera camera;
  private final Family controllerFamily =
      Family.all(GroupControllerComponent.class, PositionComponent.class).get();
  private ImmutableArray<Entity> companionEntities;
  private ImmutableArray<Entity> controllerEntities;

  private final ComponentMapper<GroupControllerComponent> controllerMapper =
      ComponentMapper.getFor(GroupControllerComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);

  public FormationVisualizationSystem(
      ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font, OrthographicCamera camera) {
    this.shapeRenderer = shapeRenderer;
    this.batch = batch;
    this.font = font;
    this.camera = camera;
  }

  @Override
  public void addedToEngine(Engine engine) {
    companionEntities =
        engine.getEntitiesFor(
            Family.all(SquadMovementComponent.class, PositionComponent.class).get());
    controllerEntities = engine.getEntitiesFor(controllerFamily);
  }

  @Override
  public void update(float deltaTime) {
    if (!hasFormationData()) {
      return;
    }

    renderFormationShapes();
    renderFormationInfo();
  }

  private boolean hasFormationData() {
    return companionEntities.size() > 0 && controllerEntities.size() > 0;
  }

  private void renderFormationShapes() {
    Entity controllerEntity = controllerEntities.first();
    PositionComponent controllerPosition = positionMapper.get(controllerEntity);
    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    renderFormationLines(controllerPosition);
    renderCompanionPositions();
    shapeRenderer.end();
  }

  private void renderFormationLines(PositionComponent controllerPosition) {
    shapeRenderer.setColor(CENTER_COLOR);
    shapeRenderer.circle(controllerPosition.x, controllerPosition.y, CENTER_CIRCLE_RADIUS);

    shapeRenderer.setColor(LINE_COLOR);
    for (Entity companion : companionEntities) {
      PositionComponent companionPosition = positionMapper.get(companion);
      shapeRenderer.line(
          controllerPosition.x, controllerPosition.y, companionPosition.x, companionPosition.y);
    }
  }

  private void renderCompanionPositions() {
    shapeRenderer.setColor(COMPANION_COLOR);
    for (Entity companion : companionEntities) {
      PositionComponent position = positionMapper.get(companion);
      shapeRenderer.circle(position.x, position.y, COMPANION_CIRCLE_RADIUS);
    }
  }

  private void renderFormationInfo() {
    Entity controllerEntity = controllerEntities.first();
    PositionComponent controllerPosition = positionMapper.get(controllerEntity);
    GroupControllerComponent controller = controllerMapper.get(controllerEntity);
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    font.setColor(FORMATION_TEXT_COLOR);
    font.draw(
        batch,
        buildFormationInfo(controller),
        infoTextX(controllerPosition),
        infoTextY(controllerPosition));
    font.draw(
        batch, ACTIVE_STATE_TEXT, stateTextX(controllerPosition), stateTextY(controllerPosition));
    batch.end();
  }

  private String buildFormationInfo(GroupControllerComponent controller) {
    return "Formation: "
        + controller.currentFormation.name()
        + " | Members: "
        + companionEntities.size();
  }

  private float infoTextX(PositionComponent controllerPosition) {
    return controllerPosition.x - FORMATION_TEXT_X_OFFSET;
  }

  private float infoTextY(PositionComponent controllerPosition) {
    return controllerPosition.y + FORMATION_TEXT_Y_OFFSET;
  }

  private float stateTextX(PositionComponent controllerPosition) {
    return controllerPosition.x - FORMATION_STATE_X_OFFSET;
  }

  private float stateTextY(PositionComponent controllerPosition) {
    return controllerPosition.y + FORMATION_STATE_Y_OFFSET;
  }
}
