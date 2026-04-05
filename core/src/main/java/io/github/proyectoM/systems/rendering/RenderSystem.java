package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.visual.OpacityComponent;
import io.github.proyectoM.utils.IsometricUtils;
import java.util.Comparator;

/** Renders animated world entities using isometric depth sorting. */
public class RenderSystem extends EntitySystem {
  private static final float SPRITE_CENTER_OFFSET = 0.5f;
  private static final float ENTITY_SCALE = 3f;
  private static final float FULL_COLOR_CHANNEL = 1f;
  private static final float NO_ROTATION = 0f;

  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<OpacityComponent> opacityMapper =
      ComponentMapper.getFor(OpacityComponent.class);
  private final ComponentMapper<DeadComponent> deadMapper =
      ComponentMapper.getFor(DeadComponent.class);
  private final SpriteBatch batch;
  private final Array<Entity> sortedEntities = new Array<>();
  private final Comparator<Entity> depthComparator = this::compareByDepth;
  private final Family family = Family.all(PositionComponent.class, AnimationComponent.class).get();

  private ImmutableArray<Entity> entities;

  public RenderSystem(SpriteBatch batch) {
    this.batch = batch;
  }

  @Override
  public void addedToEngine(Engine engine) {
    entities = engine.getEntitiesFor(family);
  }

  @Override
  public void update(float deltaTime) {
    sortEntitiesByDepth();
    batch.begin();
    renderSortedEntities();
    batch.end();
  }

  /**
   * Compares two entities for rendering order. Dead entities (corpses) always render before alive
   * entities so they appear behind living characters. Within the same alive/dead group, entities
   * are sorted by isometric depth so that farther entities render first (behind).
   *
   * @param firstEntity the first entity to compare
   * @param secondEntity the second entity to compare
   * @return negative if firstEntity should render before secondEntity, positive otherwise
   */
  private int compareByDepth(Entity firstEntity, Entity secondEntity) {
    boolean firstEntityDead = deadMapper.has(firstEntity);
    boolean secondEntityDead = deadMapper.has(secondEntity);
    if (firstEntityDead != secondEntityDead) {
      return firstEntityDead ? -1 : 1;
    }

    PositionComponent firstPosition = positionMapper.get(firstEntity);
    PositionComponent secondPosition = positionMapper.get(secondEntity);
    float firstDepth = IsometricUtils.calculateDepth(firstPosition.x, firstPosition.y);
    float secondDepth = IsometricUtils.calculateDepth(secondPosition.x, secondPosition.y);
    return Float.compare(secondDepth, firstDepth);
  }

  private void sortEntitiesByDepth() {
    sortedEntities.clear();
    for (int i = 0; i < entities.size(); i++) {
      sortedEntities.add(entities.get(i));
    }
    sortedEntities.sort(depthComparator);
  }

  private void renderSortedEntities() {
    for (int i = 0; i < sortedEntities.size; i++) {
      renderEntity(sortedEntities.get(i));
    }
  }

  private void renderEntity(Entity entity) {
    PositionComponent position = positionMapper.get(entity);
    AnimationComponent animation = animationMapper.get(entity);
    if (animation.currentAnimation == null) {
      return;
    }

    OpacityComponent opacity = opacityMapper.get(entity);
    applyOpacity(opacity);
    TextureRegion region = animation.currentAnimation.getKeyFrame(animation.stateTime);
    renderEntityVisual(position, region, ENTITY_SCALE);
    resetOpacity(opacity);
  }

  private void applyOpacity(OpacityComponent opacity) {
    if (opacity == null) {
      return;
    }

    batch.setColor(FULL_COLOR_CHANNEL, FULL_COLOR_CHANNEL, FULL_COLOR_CHANNEL, opacity.alpha);
  }

  private void resetOpacity(OpacityComponent opacity) {
    if (opacity == null) {
      return;
    }

    batch.setColor(FULL_COLOR_CHANNEL, FULL_COLOR_CHANNEL, FULL_COLOR_CHANNEL, FULL_COLOR_CHANNEL);
  }

  private void renderEntityVisual(PositionComponent position, TextureRegion region, float scale) {
    float regionWidth = region.getRegionWidth();
    float regionHeight = region.getRegionHeight();
    float centerOffsetX = regionWidth * SPRITE_CENTER_OFFSET;
    float centerOffsetY = regionHeight * SPRITE_CENTER_OFFSET;
    float drawX = position.x - centerOffsetX;
    float drawY = position.y - centerOffsetY;

    batch.draw(
        region,
        drawX,
        drawY,
        centerOffsetX,
        centerOffsetY,
        regionWidth,
        regionHeight,
        scale,
        scale,
        NO_ROTATION);
  }
}
