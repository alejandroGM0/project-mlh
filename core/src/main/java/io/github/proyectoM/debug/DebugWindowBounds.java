package io.github.proyectoM.debug;

import com.badlogic.gdx.scenes.scene2d.ui.Window;

/** Immutable window bounds used by debug layout calculations. */
final class DebugWindowBounds {
  private final int x;
  private final int y;
  private final int width;
  private final int height;

  private DebugWindowBounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  static DebugWindowBounds of(int x, int y, int width, int height) {
    return new DebugWindowBounds(x, y, width, height);
  }

  static DebugWindowBounds fromWindow(Window window) {
    return new DebugWindowBounds(
        (int) window.getX(),
        (int) window.getY(),
        (int) window.getWidth(),
        (int) window.getHeight());
  }

  int getX() {
    return x;
  }

  int getY() {
    return y;
  }

  int getWidth() {
    return width;
  }

  int getHeight() {
    return height;
  }
}
