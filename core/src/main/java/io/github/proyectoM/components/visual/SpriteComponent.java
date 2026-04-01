package io.github.proyectoM.components.visual;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores sprite rendering data for an entity. */
public class SpriteComponent implements Component, Poolable {
  public static final float DEFAULT_ANGLE = 0f;
  public static final float DEFAULT_SCALE = 1f;

  public TextureRegion texture;
  public float angle = DEFAULT_ANGLE;
  public float scale = DEFAULT_SCALE;

  public SpriteComponent() {}

  public SpriteComponent(TextureRegion texture) {
    this.texture = texture;
  }

  public SpriteComponent(TextureRegion texture, float scale) {
    this.texture = texture;
    this.scale = scale;
  }

  @Override
  public void reset() {
    texture = null;
    angle = DEFAULT_ANGLE;
    scale = DEFAULT_SCALE;
  }
}
