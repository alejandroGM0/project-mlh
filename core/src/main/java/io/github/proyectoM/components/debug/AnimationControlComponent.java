package io.github.proyectoM.components.debug;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import io.github.proyectoM.components.entity.animation.ActionStateComponent.ActionType;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent.MovementType;

/** Component to manually control/override animation states for debugging. */
public class AnimationControlComponent implements Component, Poolable {
  public boolean lockAction = false;
  public ActionType forcedAction = ActionType.IDLE;

  public boolean lockMovement = false;
  public MovementType forcedMovement = MovementType.FORWARD;

  public boolean lockDirection = false;
  public int forcedDirection = 0;

  @Override
  public void reset() {
    lockAction = false;
    forcedAction = ActionType.IDLE;
    lockMovement = false;
    forcedMovement = MovementType.FORWARD;
    lockDirection = false;
    forcedDirection = 0;
  }
}
