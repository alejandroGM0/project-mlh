package io.github.proyectoM.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/** Converts between cartesian world coordinates and 2:1 isometric screen coordinates. */
public final class IsometricUtils {
  private static final float HALF_DIVISOR = 2f;
  private static final float ISOMETRIC_ROTATION_DIVISOR = 4f;

  public static final float TILE_WIDTH = 64f;
  public static final float TILE_HEIGHT = 32f;
  public static final float TILE_WIDTH_HALF = TILE_WIDTH / HALF_DIVISOR;
  public static final float TILE_HEIGHT_HALF = TILE_HEIGHT / HALF_DIVISOR;

  private static final float ISO_ANGLE_RADIANS = MathUtils.PI / ISOMETRIC_ROTATION_DIVISOR;
  private static final float COS_45 = MathUtils.cos(ISO_ANGLE_RADIANS);
  private static final float SIN_45 = MathUtils.sin(ISO_ANGLE_RADIANS);

  private IsometricUtils() {}

  public static Vector2 worldToScreen(float worldX, float worldY) {
    float screenX = (worldX - worldY) * TILE_WIDTH_HALF;
    float screenY = (worldX + worldY) * TILE_HEIGHT_HALF;
    return new Vector2(screenX, screenY);
  }

  public static Vector2 worldToScreen(Vector2 worldPos) {
    return worldToScreen(worldPos.x, worldPos.y);
  }

  public static void worldToScreen(float worldX, float worldY, Vector2 result) {
    result.x = (worldX - worldY) * TILE_WIDTH_HALF;
    result.y = (worldX + worldY) * TILE_HEIGHT_HALF;
  }

  public static Vector2 screenToWorld(float screenX, float screenY) {
    float worldX = (screenX / TILE_WIDTH_HALF + screenY / TILE_HEIGHT_HALF) / HALF_DIVISOR;
    float worldY = (screenY / TILE_HEIGHT_HALF - screenX / TILE_WIDTH_HALF) / HALF_DIVISOR;
    return new Vector2(worldX, worldY);
  }

  public static Vector2 screenToWorld(Vector2 screenPos) {
    return screenToWorld(screenPos.x, screenPos.y);
  }

  public static void screenToWorld(float screenX, float screenY, Vector2 result) {
    result.x = (screenX / TILE_WIDTH_HALF + screenY / TILE_HEIGHT_HALF) / HALF_DIVISOR;
    result.y = (screenY / TILE_HEIGHT_HALF - screenX / TILE_WIDTH_HALF) / HALF_DIVISOR;
  }

  public static Vector2 rotateInputForIsometric(float inputX, float inputY) {
    float rotatedX = inputX * COS_45 - inputY * SIN_45;
    float rotatedY = inputX * SIN_45 + inputY * COS_45;
    return new Vector2(rotatedX, rotatedY);
  }

  public static void rotateInputForIsometric(Vector2 input, Vector2 result) {
    float rotatedX = input.x * COS_45 - input.y * SIN_45;
    float rotatedY = input.x * SIN_45 + input.y * COS_45;
    result.set(rotatedX, rotatedY);
  }

  public static float calculateDepth(float worldX, float worldY) {
    return -(worldX + worldY);
  }

  public static float calculateDepth(Vector2 worldPos) {
    return calculateDepth(worldPos.x, worldPos.y);
  }
}
