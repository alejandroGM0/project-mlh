package io.github.proyectoM.components.visual;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the atlas identifier used to resolve animations for an entity. */
public class VisualAssetComponent implements Component, Poolable {
  public String visualAssetId;

  public VisualAssetComponent() {}

  public VisualAssetComponent(String visualAssetId) {
    this.visualAssetId = visualAssetId;
  }

  @Override
  public void reset() {
    visualAssetId = null;
  }
}
