package io.github.proyectoM.debug;

/** Mutable container for debug-render toggle flags shared across the render pipeline. */
public class RenderDebugSettings {

  public boolean physicsEnabled;
  public boolean lightingEnabled = true;
  public boolean leaderMarkerEnabled = false;

  /**
   * Creates settings with the given initial physics-debug state.
   *
   * @param physicsEnabled whether Box2D wireframes are drawn initially
   */
  public RenderDebugSettings(boolean physicsEnabled) {
    this.physicsEnabled = physicsEnabled;
  }
}
