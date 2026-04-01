package io.github.proyectoM.systems.animation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.animation.AnimEventComponent;
import io.github.proyectoM.components.entity.animation.AnimEventComponent.AnimEventType;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.weapon.MuzzleFlashComponent;
import java.util.Map;

/** Advances animation time and emits frame-based animation events. */
public class AnimationSystem extends IteratingSystem {
  private final ComponentMapper<AnimationComponent> animationMapper =
      ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<AnimEventComponent> eventMapper =
      ComponentMapper.getFor(AnimEventComponent.class);

  public AnimationSystem() {
    super(Family.all(AnimationComponent.class).exclude(MuzzleFlashComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    AnimationComponent animation = animationMapper.get(entity);
    if (animation.currentAnimation == null) {
      return;
    }

    int previousFrame = animation.currentAnimation.getKeyFrameIndex(animation.stateTime);
    animation.stateTime += deltaTime;
    int currentFrame = animation.currentAnimation.getKeyFrameIndex(animation.stateTime);
    processEvents(entity, previousFrame, currentFrame, animation);
  }

  private void processEvents(
      Entity entity, int previousFrame, int currentFrame, AnimationComponent animation) {
    AnimEventComponent events = eventMapper.get(entity);
    if (events == null) {
      return;
    }

    events.clearTriggered();
    checkFrameEvents(events, previousFrame, currentFrame);
    if (animation.currentAnimation.isAnimationFinished(animation.stateTime)) {
      events.endTriggered = true;
    }
  }

  private void checkFrameEvents(AnimEventComponent events, int previousFrame, int currentFrame) {
    for (Map.Entry<AnimEventType, Integer> entry : events.eventFrames.entrySet()) {
      int eventFrame = entry.getValue();
      if (previousFrame < eventFrame && currentFrame >= eventFrame) {
        events.triggeredEvents.add(entry.getKey());
      }
    }
  }
}
