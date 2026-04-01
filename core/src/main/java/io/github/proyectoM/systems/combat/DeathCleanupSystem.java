package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.visual.OpacityComponent;

/** Fades out corpses and removes them after their death lifetime expires. */
public class DeathCleanupSystem extends IteratingSystem {
  private static final float CORPSE_DURATION = 60f;
  private static final float FADE_START_TIME = 8f;
  private static final float MIN_ALPHA = 0f;
  private static final float FULL_ALPHA = 1f;

  private final ComponentMapper<DeadComponent> deadMapper =
      ComponentMapper.getFor(DeadComponent.class);
  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<OpacityComponent> opacityMapper =
      ComponentMapper.getFor(OpacityComponent.class);

  public DeathCleanupSystem() {
    super(Family.all(DeadComponent.class, AnimationComponent.class, OpacityComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    DeadComponent dead = deadMapper.get(entity);
    AnimationComponent animation = animationMapper.get(entity);
    OpacityComponent opacity = opacityMapper.get(entity);

    dead.timeSinceDeath += deltaTime;
    updateAnimationFinished(dead, animation);
    updateFadeEffect(dead, opacity);
    if (dead.timeSinceDeath >= CORPSE_DURATION) {
      getEngine().removeEntity(entity);
    }
  }

  private void updateAnimationFinished(DeadComponent dead, AnimationComponent animation) {
    if (!dead.animationFinished && animation.currentAnimation != null) {
      dead.animationFinished = animation.currentAnimation.isAnimationFinished(animation.stateTime);
    }
  }

  private void updateFadeEffect(DeadComponent dead, OpacityComponent opacity) {
    if (dead.timeSinceDeath <= FADE_START_TIME) {
      return;
    }

    float fadeProgress =
        (dead.timeSinceDeath - FADE_START_TIME) / (CORPSE_DURATION - FADE_START_TIME);
    opacity.alpha = Math.max(MIN_ALPHA, FULL_ALPHA - fadeProgress);
  }
}
