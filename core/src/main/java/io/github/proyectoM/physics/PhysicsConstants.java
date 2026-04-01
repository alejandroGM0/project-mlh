package io.github.proyectoM.physics;

/** Contains physical conversion constants for the game. */
public final class PhysicsConstants {
  /** The number of pixels that represent one meter in the game world. */
  public static final float PIXELS_PER_METER = 32f;

  /** The number of meters that represent one pixel in the game world. */
  public static final float METERS_PER_PIXEL = 1f / PIXELS_PER_METER;

  private PhysicsConstants() {}
}
