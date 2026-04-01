package io.github.proyectoM.debug;

import com.badlogic.gdx.scenes.scene2d.ui.Window;

/** Couples a debug panel with its window and optional category metadata. */
final class DebugPanelRegistration {
  private final DebugPanel panel;
  private final Window window;
  private final String category;

  DebugPanelRegistration(DebugPanel panel, Window window, String category) {
    this.panel = panel;
    this.window = window;
    this.category = category;
  }

  String getTitle() {
    return panel.getTitle();
  }

  DebugPanel getPanel() {
    return panel;
  }

  Window getWindow() {
    return window;
  }

  String getCategory() {
    return category;
  }

  boolean isActive() {
    return panel.isActive();
  }

  boolean hasTitle(String title) {
    return panel.getTitle().equals(title);
  }
}
