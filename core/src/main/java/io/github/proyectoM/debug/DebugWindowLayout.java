package io.github.proyectoM.debug;

import java.util.List;

/** Computes non-overlapping positions for debug windows. */
final class DebugWindowLayout {
  private final int windowMargin;
  private final int windowPadding;

  DebugWindowLayout(int windowMargin, int windowPadding) {
    this.windowMargin = windowMargin;
    this.windowPadding = windowPadding;
  }

  DebugWindowBounds findAvailableBounds(
      List<DebugWindowBounds> occupiedBounds,
      int width,
      int height,
      int screenWidth,
      int screenHeight) {
    int maxX = screenWidth - width - windowMargin;
    int maxY = screenHeight - height - windowMargin;

    for (int y = windowMargin; y <= maxY; y += windowPadding) {
      for (int x = windowMargin; x <= maxX; x += windowPadding) {
        DebugWindowBounds candidateBounds = DebugWindowBounds.of(x, y, width, height);
        if (!overlapsAny(candidateBounds, occupiedBounds)) {
          return candidateBounds;
        }
      }
    }

    return DebugWindowBounds.of(windowMargin, windowMargin, width, height);
  }

  static boolean rectanglesOverlap(DebugWindowBounds first, DebugWindowBounds second) {
    return first.getX() < second.getX() + second.getWidth()
        && first.getX() + first.getWidth() > second.getX()
        && first.getY() < second.getY() + second.getHeight()
        && first.getY() + first.getHeight() > second.getY();
  }

  private boolean overlapsAny(
      DebugWindowBounds candidateBounds, List<DebugWindowBounds> occupiedBounds) {
    for (DebugWindowBounds occupiedBoundsEntry : occupiedBounds) {
      if (rectanglesOverlap(candidateBounds, occupiedBoundsEntry)) {
        return true;
      }
    }
    return false;
  }
}
