package io.github.proyectoM.systems.animation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.proyectoM.animation.AnimationKey;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.visual.VisualAssetComponent;
import io.github.proyectoM.registry.VisualAssetRegistry;

/** Selects the best animation for each entity from its current state. */
public class AnimationSelectionSystem extends IteratingSystem {
  private static final int DEFAULT_VARIANT = 0;
  private static final float ANIMATION_START_TIME = 0f;

  private final ComponentMapper<ActionStateComponent> actionMapper =
      ComponentMapper.getFor(ActionStateComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);
  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<VisualAssetComponent> visualAssetMapper =
      ComponentMapper.getFor(VisualAssetComponent.class);

  public AnimationSelectionSystem() {
    super(
        Family.all(
                ActionStateComponent.class,
                MovementDirectionStateComponent.class,
                VisualAssetComponent.class,
                AnimationComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    ActionStateComponent actionState = actionMapper.get(entity);
    MovementDirectionStateComponent movementDirection = movementMapper.get(entity);
    VisualAssetComponent visualAsset = visualAssetMapper.get(entity);
    AnimationComponent animationComponent = animationMapper.get(entity);

    Animation<TextureRegion> targetAnimation =
        selectAnimation(visualAsset.visualAssetId, actionState, movementDirection);
    if (targetAnimation == null || animationComponent.currentAnimation == targetAnimation) {
      return;
    }

    animationComponent.currentAnimation = targetAnimation;
    animationComponent.stateTime = ANIMATION_START_TIME;
  }

  private Animation<TextureRegion> selectAnimation(
      String atlasPath,
      ActionStateComponent actionState,
      MovementDirectionStateComponent movementDirection) {
    int variant = actionState.getCurrentVariant();
    AnimationKey requestedKey =
        AnimationKey.get(
            actionState.actionType,
            movementDirection.movementType,
            movementDirection.directionIndex,
            variant);

    Animation<TextureRegion> targetAnimation =
        VisualAssetRegistry.getAnimation(atlasPath, requestedKey);
    if (targetAnimation != null || variant == DEFAULT_VARIANT) {
      return targetAnimation;
    }

    AnimationKey fallbackKey =
        AnimationKey.get(
            actionState.actionType,
            movementDirection.movementType,
            movementDirection.directionIndex,
            DEFAULT_VARIANT);
    return VisualAssetRegistry.getAnimation(atlasPath, fallbackKey);
  }
}
