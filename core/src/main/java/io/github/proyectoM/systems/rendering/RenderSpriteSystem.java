package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.visual.SpriteComponent;

/** Renders entities that use a static sprite region. */
public class RenderSpriteSystem extends IteratingSystem {
  private static final float SPRITE_CENTER_OFFSET = 0.5f;

  private final SpriteBatch batch;
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<SpriteComponent> spriteMapper =
      ComponentMapper.getFor(SpriteComponent.class);

  public RenderSpriteSystem(SpriteBatch batch) {
    super(Family.all(PositionComponent.class, SpriteComponent.class).get());
    this.batch = batch;
  }

  @Override
  public void update(float deltaTime) {
    batch.begin();
    super.update(deltaTime);
    batch.end();
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PositionComponent position = positionMapper.get(entity);
    SpriteComponent sprite = spriteMapper.get(entity);
    if (sprite.texture == null) {
      return;
    }

    renderSprite(position, sprite);
  }

  private void renderSprite(PositionComponent position, SpriteComponent sprite) {
    TextureRegion region = sprite.texture;
    float width = region.getRegionWidth();
    float height = region.getRegionHeight();
    float originX = width * SPRITE_CENTER_OFFSET;
    float originY = height * SPRITE_CENTER_OFFSET;
    float drawX = position.x - originX;
    float drawY = position.y - originY;

    batch.draw(
        region,
        drawX,
        drawY,
        originX,
        originY,
        width,
        height,
        sprite.scale,
        sprite.scale,
        sprite.angle);
  }
}
