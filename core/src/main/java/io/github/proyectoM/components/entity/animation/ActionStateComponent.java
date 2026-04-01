package io.github.proyectoM.components.entity.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import java.util.EnumMap;
import java.util.Map;

/** Stores the current action state and per-action animation variants for an entity. */
public class ActionStateComponent implements Component, Poolable {
  private static final int DEFAULT_VARIANT = 0;

  public ActionType actionType = ActionType.IDLE;
  public float actionTime = 0f;
  public final Map<ActionType, Integer> variantByAction = new EnumMap<>(ActionType.class);

  public int getVariant(ActionType action) {
    return variantByAction.getOrDefault(action, DEFAULT_VARIANT);
  }

  public void setVariant(ActionType action, int variant) {
    variantByAction.put(action, variant);
  }

  public int getCurrentVariant() {
    return getVariant(actionType);
  }

  @Override
  public void reset() {
    actionType = ActionType.IDLE;
    actionTime = 0f;
    variantByAction.clear();
  }

  /** Enumerates the action states supported by the animation systems. */
  public enum ActionType {
    IDLE,
    MOVE,
    ATTACK,
    RELOAD,
    DIE,
    HURT,
    SPECIAL
  }
}
