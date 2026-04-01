package io.github.proyectoM.components.entity.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Marker component that freezes movement and rotation during one-shot animations. */
public class AnimationLockComponent implements Component, Poolable {
  @Override
  public void reset() {}
}
