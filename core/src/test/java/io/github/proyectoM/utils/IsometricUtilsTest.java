package io.github.proyectoM.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

class IsometricUtilsTest {

  private static final float EPSILON = 0.001f;

  /**
   * Verifies that the origin (0,0) maps to the screen origin.
   */
  @Test
  void worldToScreen_origin_returnsOrigin() {
    Vector2 result = IsometricUtils.worldToScreen(0f, 0f);

    assertEquals(0f, result.x, EPSILON);
    assertEquals(0f, result.y, EPSILON);
  }

  /**
   * Verifies that worldToScreen calculates correctly for positive coordinates.
   */
  @Test
  void worldToScreen_positiveCoordinates_calculatesCorrectly() {
    Vector2 result = IsometricUtils.worldToScreen(1f, 0f);

    assertEquals(IsometricUtils.TILE_WIDTH_HALF, result.x, EPSILON);
    assertEquals(IsometricUtils.TILE_HEIGHT_HALF, result.y, EPSILON);
  }

  /**
   * Verifies that worldToScreen with only positive Y inverts the screen X coordinate.
   */
  @Test
  void worldToScreen_yOnly_invertedScreenX() {
    Vector2 result = IsometricUtils.worldToScreen(0f, 1f);

    assertEquals(-IsometricUtils.TILE_WIDTH_HALF, result.x, EPSILON);
    assertEquals(IsometricUtils.TILE_HEIGHT_HALF, result.y, EPSILON);
  }

  /**
   * Verifies the worldToScreen variant that receives a Vector2.
   */
  @Test
  void worldToScreen_vector2Overload_matchesFloatOverload() {
    Vector2 fromFloats = IsometricUtils.worldToScreen(3f, 7f);
    Vector2 fromVector = IsometricUtils.worldToScreen(new Vector2(3f, 7f));

    assertEquals(fromFloats.x, fromVector.x, EPSILON);
    assertEquals(fromFloats.y, fromVector.y, EPSILON);
  }

  /**
   * Verifies the worldToScreen variant that writes to a result Vector2.
   */
  @Test
  void worldToScreen_resultOverload_populatesResult() {
    Vector2 result = new Vector2();
    IsometricUtils.worldToScreen(2f, 5f, result);

    Vector2 expected = IsometricUtils.worldToScreen(2f, 5f);
    assertEquals(expected.x, result.x, EPSILON);
    assertEquals(expected.y, result.y, EPSILON);
  }

  /**
   * Verifies that screenToWorld at the origin returns the origin.
   */
  @Test
  void screenToWorld_origin_returnsOrigin() {
    Vector2 result = IsometricUtils.screenToWorld(0f, 0f);

    assertEquals(0f, result.x, EPSILON);
    assertEquals(0f, result.y, EPSILON);
  }

  /**
   * Verifies that the world-to-screen-to-world round trip preserves coordinates.
   */
  @Test
  void roundTrip_worldToScreenToWorld_preservesCoordinates() {
    float worldX = 4.5f;
    float worldY = -2.3f;

    Vector2 screen = IsometricUtils.worldToScreen(worldX, worldY);
    Vector2 world = IsometricUtils.screenToWorld(screen.x, screen.y);

    assertEquals(worldX, world.x, EPSILON);
    assertEquals(worldY, world.y, EPSILON);
  }

  /**
   * Verifies that the screen-to-world-to-screen round trip preserves coordinates.
   */
  @Test
  void roundTrip_screenToWorldToScreen_preservesCoordinates() {
    float screenX = 128f;
    float screenY = 64f;

    Vector2 world = IsometricUtils.screenToWorld(screenX, screenY);
    Vector2 screen = IsometricUtils.worldToScreen(world.x, world.y);

    assertEquals(screenX, screen.x, EPSILON);
    assertEquals(screenY, screen.y, EPSILON);
  }

  /**
   * Verifies the screenToWorld variant that receives a result Vector2.
   */
  @Test
  void screenToWorld_resultOverload_matchesFloatOverload() {
    Vector2 result = new Vector2();
    IsometricUtils.screenToWorld(100f, 50f, result);

    Vector2 expected = IsometricUtils.screenToWorld(100f, 50f);
    assertEquals(expected.x, result.x, EPSILON);
    assertEquals(expected.y, result.y, EPSILON);
  }

  /**
   * Verifies that calculateDepth returns the negated sum of world coordinates.
   */
  @Test
  void calculateDepth_returnsNegativeSum() {
    float depth = IsometricUtils.calculateDepth(3f, 4f);

    assertEquals(-(3f + 4f), depth, EPSILON);
  }

  /**
   * Verifies that the Vector2 variant of calculateDepth matches the float variant.
   */
  @Test
  void calculateDepth_vector2Overload_matchesFloatOverload() {
    float fromFloats = IsometricUtils.calculateDepth(5f, -2f);
    float fromVector = IsometricUtils.calculateDepth(new Vector2(5f, -2f));

    assertEquals(fromFloats, fromVector, EPSILON);
  }

  /**
   * Verifies that northern objects have greater depth than southern ones.
   */
  @Test
  void calculateDepth_northernObjects_haveHigherDepthThanSouthern() {
    float depthNorth = IsometricUtils.calculateDepth(10f, 10f);
    float depthSouth = IsometricUtils.calculateDepth(1f, 1f);

    assert depthNorth < depthSouth;
  }

  /**
   * Verifies that rotateInputForIsometric preserves the vector magnitude.
   */
  @Test
  void rotateInputForIsometric_preservesMagnitude() {
    float inputX = 3f;
    float inputY = 4f;
    float originalMagnitude = (float) Math.sqrt(inputX * inputX + inputY * inputY);

    Vector2 rotated = IsometricUtils.rotateInputForIsometric(inputX, inputY);
    float rotatedMagnitude = rotated.len();

    assertEquals(originalMagnitude, rotatedMagnitude, EPSILON);
  }

  /**
   * Verifies that the Vector2 result overload of rotateInputForIsometric matches the return overload.
   */
  @Test
  void rotateInputForIsometric_resultOverload_matchesReturnOverload() {
    Vector2 input = new Vector2(1f, 0f);
    Vector2 result = new Vector2();

    IsometricUtils.rotateInputForIsometric(input, result);
    Vector2 expected = IsometricUtils.rotateInputForIsometric(input.x, input.y);

    assertEquals(expected.x, result.x, EPSILON);
    assertEquals(expected.y, result.y, EPSILON);
  }

  /**
   * Verifies that rotateInputForIsometric with zero vector returns zero.
   */
  @Test
  void rotateInputForIsometric_zeroInput_returnsZero() {
    Vector2 result = IsometricUtils.rotateInputForIsometric(0f, 0f);

    assertEquals(0f, result.x, EPSILON);
    assertEquals(0f, result.y, EPSILON);
  }
}