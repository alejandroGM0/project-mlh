package io.github.proyectoM.utils;

import com.badlogic.gdx.math.MathUtils;

/** Utility methods for snapping and indexing eight-way isometric directions. */
public final class DirectionUtils {
  public static final float DIRECTION_SECTOR_SIZE_DEGREES = 45f;
  public static final float DIRECTION_SECTOR_OFFSET_DEGREES = 22.5f;
  public static final float FULL_CIRCLE_DEGREES = 360f;
  public static final int NUMBER_OF_DIRECTIONS = 8;
  private static final float ISOMETRIC_ANGLE_DIVISOR = 4f;

  public static final float ISOMETRIC_ANGLE_OFFSET_RADIANS = MathUtils.PI / ISOMETRIC_ANGLE_DIVISOR;

  private DirectionUtils() {}

  /**
   * Calculates the direction index (0-7) based on the angle in radians.
   *
   * @param angleRad The angle in radians.
   * @return The direction index (0-7).
   */
  public static int calculateDirectionIndex(float angleRad) {
    float cartesianAngle = angleRad + ISOMETRIC_ANGLE_OFFSET_RADIANS;
    float normalizedDegrees = normalizeAngleToDegrees(cartesianAngle);
    return (NUMBER_OF_DIRECTIONS - determineSectorIndex(normalizedDegrees)) % NUMBER_OF_DIRECTIONS;
  }

  /**
   * Snaps an angle (in degrees) to the nearest 45-degree increment (center of the sector).
   *
   * @param angleDeg Input angle in degrees.
   * @return Snapped angle in degrees (0, 45, 90, etc).
   */
  public static float snapToNearestDirection(float angleDeg) {
    float normalized = normalizeAngleToDegrees(MathUtils.degreesToRadians * angleDeg);
    int sectorIndex = determineSectorIndex(normalized);
    return sectorIndex * DIRECTION_SECTOR_SIZE_DEGREES;
  }

  /**
   * Normalizes the angle to degrees in the range [0, 360).
   *
   * @param angleRad The angle in radians.
   * @return The normalized angle in degrees.
   */
  public static float normalizeAngleToDegrees(float angleRad) {
    float degrees = (float) Math.toDegrees(angleRad);
    while (degrees < 0) {
      degrees += FULL_CIRCLE_DEGREES;
    }
    while (degrees >= FULL_CIRCLE_DEGREES) {
      degrees -= FULL_CIRCLE_DEGREES;
    }
    return degrees;
  }

  /**
   * Determines the sector index based on normalized degrees.
   *
   * @param normalizedDegrees The normalized angle in degrees.
   * @return The sector index.
   */
  public static int determineSectorIndex(float normalizedDegrees) {
    float adjustedDegrees = normalizedDegrees + DIRECTION_SECTOR_OFFSET_DEGREES;
    if (adjustedDegrees >= FULL_CIRCLE_DEGREES) {
      adjustedDegrees -= FULL_CIRCLE_DEGREES;
    }
    return (int) (adjustedDegrees / DIRECTION_SECTOR_SIZE_DEGREES) % NUMBER_OF_DIRECTIONS;
  }
}
