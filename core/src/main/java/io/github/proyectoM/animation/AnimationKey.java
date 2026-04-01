package io.github.proyectoM.animation;

import io.github.proyectoM.components.entity.animation.ActionStateComponent.ActionType;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent.MovementType;
import java.util.Objects;

/** Identifies a cached animation by action, movement type, direction, and variant. */
public final class AnimationKey {
  private static final int EXPECTED_ACTION_TYPE_COUNT = 7;
  private static final int EXPECTED_MOVEMENT_TYPE_COUNT = 4;
  private static final int DIRECTION_COUNT = 8;
  private static final int MAX_VARIANT_COUNT = 10;

  private static final int ACTION_COUNT = ActionType.values().length;
  private static final int MOVEMENT_COUNT = MovementType.values().length;

  private static final AnimationKey[][][] BASE_KEYS =
      new AnimationKey[ACTION_COUNT][MOVEMENT_COUNT][DIRECTION_COUNT];
  private static final AnimationKey[][][][] VARIANT_KEYS =
      new AnimationKey[ACTION_COUNT][MOVEMENT_COUNT][DIRECTION_COUNT][MAX_VARIANT_COUNT];

  public final ActionType actionType;
  public final MovementType movementType;
  public final int directionIndex;
  public final int animationVariant;

  static {
    validateEnumSizes();
    populateBaseKeys();
  }

  private AnimationKey(
      ActionType actionType, MovementType movementType, int directionIndex, int animationVariant) {
    this.actionType = actionType;
    this.movementType = movementType;
    this.directionIndex = directionIndex;
    this.animationVariant = animationVariant;
  }

  public static AnimationKey get(
      ActionType actionType, MovementType movementType, int directionIndex) {
    return get(actionType, movementType, directionIndex, 0);
  }

  public static AnimationKey get(
      ActionType actionType, MovementType movementType, int directionIndex, int variant) {
    validateDirectionIndex(directionIndex);
    validateVariant(variant);

    int actionIndex = actionType.ordinal();
    int movementIndex = movementType.ordinal();
    AnimationKey key = VARIANT_KEYS[actionIndex][movementIndex][directionIndex][variant];
    if (key != null) {
      return key;
    }

    key = new AnimationKey(actionType, movementType, directionIndex, variant);
    VARIANT_KEYS[actionIndex][movementIndex][directionIndex][variant] = key;
    return key;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AnimationKey)) {
      return false;
    }

    AnimationKey that = (AnimationKey) obj;
    return directionIndex == that.directionIndex
        && animationVariant == that.animationVariant
        && actionType == that.actionType
        && movementType == that.movementType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionType, movementType, directionIndex, animationVariant);
  }

  @Override
  public String toString() {
    return String.format(
        "AnimationKey{action=%s, movement=%s, dir=%d, variant=%d}",
        actionType, movementType, directionIndex, animationVariant);
  }

  private static void validateEnumSizes() {
    if (ACTION_COUNT != EXPECTED_ACTION_TYPE_COUNT) {
      throw new IllegalStateException(
          "Expected " + EXPECTED_ACTION_TYPE_COUNT + " ActionTypes, but found: " + ACTION_COUNT);
    }
    if (MOVEMENT_COUNT != EXPECTED_MOVEMENT_TYPE_COUNT) {
      throw new IllegalStateException(
          "Expected "
              + EXPECTED_MOVEMENT_TYPE_COUNT
              + " MovementTypes, but found: "
              + MOVEMENT_COUNT);
    }
  }

  private static void populateBaseKeys() {
    for (ActionType actionType : ActionType.values()) {
      for (MovementType movementType : MovementType.values()) {
        for (int directionIndex = 0; directionIndex < DIRECTION_COUNT; directionIndex++) {
          AnimationKey key = new AnimationKey(actionType, movementType, directionIndex, 0);
          int actionIndex = actionType.ordinal();
          int movementIndex = movementType.ordinal();
          BASE_KEYS[actionIndex][movementIndex][directionIndex] = key;
          VARIANT_KEYS[actionIndex][movementIndex][directionIndex][0] = key;
        }
      }
    }
  }

  private static void validateDirectionIndex(int directionIndex) {
    if (directionIndex < 0 || directionIndex >= DIRECTION_COUNT) {
      throw new IllegalArgumentException(
          "directionIndex must be between 0 and "
              + (DIRECTION_COUNT - 1)
              + ", but received: "
              + directionIndex);
    }
  }

  private static void validateVariant(int variant) {
    if (variant < 0 || variant >= MAX_VARIANT_COUNT) {
      throw new IllegalArgumentException(
          "variant must be between 0 and "
              + (MAX_VARIANT_COUNT - 1)
              + ", but received: "
              + variant);
    }
  }
}
