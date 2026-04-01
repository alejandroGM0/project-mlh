package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;

/** Draws health bars above living entities. */
public class HealthBarRenderSystem extends IteratingSystem implements Disposable {
  private static final float BAR_WIDTH = 40f;
  private static final float BAR_HEIGHT = 4f;
  private static final float BAR_VERTICAL_OFFSET = 8f;
  private static final float HALF_SIZE_DIVISOR = 2f;
  private static final float EMPTY_HEALTH = 0f;
  private static final float FULL_HEALTH = 1f;
  private static final Color BAR_BACKGROUND_COLOR = Color.DARK_GRAY;
  private static final Color BAR_FILL_COLOR = Color.GREEN;
  private static final Color DEFAULT_RENDER_COLOR = Color.WHITE;

  private final SpriteBatch batch;
  private final ShapeRenderer shapeRenderer = new ShapeRenderer();
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<HealthComponent> healthMapper =
      ComponentMapper.getFor(HealthComponent.class);
  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);

  public HealthBarRenderSystem(SpriteBatch batch) {
    super(
        Family.all(PositionComponent.class, HealthComponent.class, AnimationComponent.class)
            .exclude(DeadComponent.class)
            .get());
    this.batch = batch;
  }

  @Override
  public void update(float deltaTime) {
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    super.update(deltaTime);
    shapeRenderer.end();
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PositionComponent position = positionMapper.get(entity);
    HealthComponent health = healthMapper.get(entity);
    float regionHeight = getRegionHeight(animationMapper.get(entity));
    float barX = calculateBarX(position.x);
    float barY = calculateBarY(position.y, regionHeight);
    float healthPercent = calculateHealthPercent(health);

    drawHealthBarBackground(barX, barY);
    drawHealthBarFill(barX, barY, healthPercent);
    shapeRenderer.setColor(DEFAULT_RENDER_COLOR);
  }

  private float getRegionHeight(AnimationComponent animation) {
    if (animation.currentAnimation == null) {
      return 0f;
    }

    TextureRegion currentFrame = animation.currentAnimation.getKeyFrame(animation.stateTime);
    return currentFrame.getRegionHeight();
  }

  private float calculateBarX(float entityX) {
    return entityX - BAR_WIDTH / HALF_SIZE_DIVISOR;
  }

  private float calculateBarY(float entityY, float regionHeight) {
    return entityY + (regionHeight / HALF_SIZE_DIVISOR) + BAR_VERTICAL_OFFSET;
  }

  private float calculateHealthPercent(HealthComponent health) {
    if (health.maxHealth <= 0) {
      return EMPTY_HEALTH;
    }

    return MathUtils.clamp(
        (float) health.currentHealth / health.maxHealth, EMPTY_HEALTH, FULL_HEALTH);
  }

  private void drawHealthBarBackground(float x, float y) {
    shapeRenderer.setColor(BAR_BACKGROUND_COLOR);
    shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);
  }

  private void drawHealthBarFill(float x, float y, float percent) {
    shapeRenderer.setColor(BAR_FILL_COLOR);
    shapeRenderer.rect(x, y, BAR_WIDTH * percent, BAR_HEIGHT);
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
  }
}
