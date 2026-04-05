package io.github.proyectoM.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.MathUtils;
import org.junit.jupiter.api.Test;

class DirectionUtilsTest {

  private static final float EPSILON = 0.01f;

  /**
   * Verifies that an angle of 0 radians returns a valid direction index (0-7).
   */
  @Test
  void calculateDirectionIndex_zeroAngle_returnsValidIndex() {
    int index = DirectionUtils.calculateDirectionIndex(0f);

    assertTrue(index >= 0 && index < DirectionUtils.NUMBER_OF_DIRECTIONS);
  }

  /**
   * Verifies that all 8 equidistant sectors produce distinct indices.
   */
  @Test
  void calculateDirectionIndex_eightCardinalAngles_producesAllIndices() {
    boolean[] seen = new boolean[DirectionUtils.NUMBER_OF_DIRECTIONS];
    float sectorRad = MathUtils.PI2 / DirectionUtils.NUMBER_OF_DIRECTIONS;

    for (int i = 0; i < DirectionUtils.NUMBER_OF_DIRECTIONS; i++) {
      float angle = i * sectorRad;
      int index = DirectionUtils.calculateDirectionIndex(angle);
      assertTrue(index >= 0 && index < DirectionUtils.NUMBER_OF_DIRECTIONS,
          "Index out of range for angle " + angle + ": " + index);
      seen[index] = true;
    }

    for (int i = 0; i < DirectionUtils.NUMBER_OF_DIRECTIONS; i++) {
      assertTrue(seen[i], "Direction index " + i + " was never assigned");
    }
  }

  /**
   * Verifies that opposite angles (PI difference) produce different indices.
   */
  @Test
  void calculateDirectionIndex_oppositeAngles_produceDifferentIndices() {
    int index0 = DirectionUtils.calculateDirectionIndex(0f);
    int indexPi = DirectionUtils.calculateDirectionIndex(MathUtils.PI);

    assertTrue(index0 != indexPi);
  }

  /**
   * Verifies that snapToNearestDirection with 0 degrees returns 0.
   */
  @Test
  void snapToNearestDirection_zeroDegrees_returnsZero() {
    float snapped = DirectionUtils.snapToNearestDirection(0f);

    assertEquals(0f, snapped, EPSILON);
  }

  /**
   * Verifies that snapToNearestDirection with 90 degrees returns 90.
   */
  @Test
  void snapToNearestDirection_ninety_returnsNinety() {
    float snapped = DirectionUtils.snapToNearestDirection(90f);

    assertEquals(90f, snapped, EPSILON);
  }

  /**
   * Verifies that snapToNearestDirection with 30 degrees snaps to 45 (nearest sector).
   */
  @Test
  void snapToNearestDirection_thirtyDegrees_snapsToFortyFive() {
    float snapped = DirectionUtils.snapToNearestDirection(30f);

    assertEquals(45f, snapped, EPSILON);
  }

  /**
   * Verifies that snapToNearestDirection with 350 degrees wraps to 0.
   */
  @Test
  void snapToNearestDirection_nearFullCircle_snapsToZero() {
    float snapped = DirectionUtils.snapToNearestDirection(350f);

    assertEquals(0f, snapped, EPSILON);
  }

  /**
   * Verifies that all snap results are multiples of 45 degrees.
   */
  @Test
  void snapToNearestDirection_anyAngle_returnsMultipleOf45() {
    for (int deg = 0; deg < 360; deg += 7) {
      float snapped = DirectionUtils.snapToNearestDirection(deg);
      float remainder = snapped % DirectionUtils.DIRECTION_SECTOR_SIZE_DEGREES;

      assertEquals(0f, remainder, EPSILON,
          "Angle " + deg + " snapped to " + snapped + " which is not a multiple of 45");
    }
  }

  /**
   * Verifies that normalizeAngleToDegrees with 0 radians returns 0 degrees.
   */
  @Test
  void normalizeAngleToDegrees_zero_returnsZero() {
    float result = DirectionUtils.normalizeAngleToDegrees(0f);

    assertEquals(0f, result, EPSILON);
  }

  /**
   * Verifies that normalizeAngleToDegrees with PI radians returns 180 degrees.
   */
  @Test
  void normalizeAngleToDegrees_pi_returns180() {
    float result = DirectionUtils.normalizeAngleToDegrees(MathUtils.PI);

    assertEquals(180f, result, EPSILON);
  }

  /**
   * Verifies that normalizeAngleToDegrees with negative angles returns range [0, 360).
   */
  @Test
  void normalizeAngleToDegrees_negativeAngle_returnsPositive() {
    float result = DirectionUtils.normalizeAngleToDegrees(-MathUtils.PI / 2f);

    assertTrue(result >= 0f && result < 360f);
    assertEquals(270f, result, EPSILON);
  }

  /**
   * Verifies that normalizeAngleToDegrees with angles greater than 2*PI returns range [0, 360).
   */
  @Test
  void normalizeAngleToDegrees_overTwoPi_wrapsCorrectly() {
    float result = DirectionUtils.normalizeAngleToDegrees(MathUtils.PI2 + MathUtils.PI / 2f);

    assertTrue(result >= 0f && result < 360f);
    assertEquals(90f, result, EPSILON);
  }

  /**
   * Verifies that determineSectorIndex with 0 degrees returns sector 0.
   */
  @Test
  void determineSectorIndex_zero_returnsSectorZero() {
    int sector = DirectionUtils.determineSectorIndex(0f);

    assertEquals(0, sector);
  }

  /**
   * Verifies that determineSectorIndex with 90 degrees returns sector 2.
   */
  @Test
  void determineSectorIndex_ninety_returnsSectorTwo() {
    int sector = DirectionUtils.determineSectorIndex(90f);

    assertEquals(2, sector);
  }

  /**
   * Verifies that the sector index is always in the range [0, 7].
   */
  @Test
  void determineSectorIndex_anyValidDegrees_returnsValidRange() {
    for (int deg = 0; deg < 360; deg++) {
      int sector = DirectionUtils.determineSectorIndex(deg);

      assertTrue(sector >= 0 && sector < DirectionUtils.NUMBER_OF_DIRECTIONS,
          "Sector out of range for " + deg + " degrees: " + sector);
    }
  }
}
