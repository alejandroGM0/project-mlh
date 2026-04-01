package io.github.proyectoM.components.entity.visual;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the alpha used when rendering an entity. */
public class OpacityComponent implements Component, Poolable {
  public static final float DEFAULT_ALPHA = 1f;

  public float alpha = DEFAULT_ALPHA;

  @Override
  public void reset() {
    alpha = DEFAULT_ALPHA;
  }
}
