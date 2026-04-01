package io.github.proyectoM.components.sound;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Marks an entity as an audio listener for spatial sound. */
public class SoundListenerComponent implements Component, Poolable {
  public static final boolean DEFAULT_ACTIVE = true;

  public boolean active = DEFAULT_ACTIVE;

  public SoundListenerComponent() {}

  public SoundListenerComponent(boolean active) {
    this.active = active;
  }

  @Override
  public void reset() {
    active = DEFAULT_ACTIVE;
  }
}
