package io.github.proyectoM.components.entity.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Holds the animation currently being rendered for an entity. */
public class AnimationComponent implements Component, Poolable {
  public Animation<TextureRegion> currentAnimation;
  public float stateTime = 0f;
  public int remainingFlashFrames = 0;

  @Override
  public void reset() {
    currentAnimation = null;
    stateTime = 0f;
    remainingFlashFrames = 0;
  }
}
