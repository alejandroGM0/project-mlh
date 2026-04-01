package io.github.proyectoM.components.companion;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores per-companion formation data inside the squad. */
public class SquadMovementComponent implements Component, Poolable {
  public static final int DEFAULT_MEMBER_INDEX = 0;

  public int memberIndex = DEFAULT_MEMBER_INDEX;
  public final Vector2 formationOffset = new Vector2();

  @Override
  public void reset() {
    memberIndex = DEFAULT_MEMBER_INDEX;
    formationOffset.setZero();
  }
}
