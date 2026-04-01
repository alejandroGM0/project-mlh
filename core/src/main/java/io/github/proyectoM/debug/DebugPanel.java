package io.github.proyectoM.debug;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** An interface for modular debug panels. */
public interface DebugPanel {
  /**
   * Gets the title of the debug panel.
   *
   * @return The title.
   */
  String getTitle();

  /**
   * Updates the debug panel.
   *
   * @param delta The time in seconds since the last update.
   */
  void update(float delta);

  /**
   * Checks if the debug panel is active.
   *
   * @return true if the panel is active, false otherwise.
   */
  boolean isActive();

  /**
   * Sets the active state of the debug panel.
   *
   * @param active The new active state.
   */
  void setActive(boolean active);

  /**
   * Builds the UI for the debug panel.
   *
   * @param skin The skin to use for the UI.
   * @return The root actor of the panel's UI.
   */
  Actor buildPanel(Skin skin);

  /**
   * An optional render method that panels can implement if they need to draw elements with a
   * ShapeRenderer (e.g., overlays in the world). A default empty implementation is provided to
   * avoid forcing changes in panels that do not draw.
   *
   * @param shapeRenderer The ShapeRenderer shared by the debug system.
   */
  default void render(ShapeRenderer shapeRenderer) {}
}
