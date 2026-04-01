package io.github.proyectoM.components.companion;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores group-wide movement and formation settings for companions. */
public class GroupControllerComponent implements Component, Poolable {
  public static final float DEFAULT_MOVEMENT_SPEED = 400f;
  public static final float DEFAULT_FORMATION_SPACING = 240f;
  public static final boolean DEFAULT_FORMATION_CHANGED = false;
  public static final FormationType DEFAULT_FORMATION = FormationType.LINE;

  public enum FormationType {
    LINE,
    COLUMN,
    CIRCLE,
    V_FORMATION,
    SQUARE
  }

  public float movementSpeed = DEFAULT_MOVEMENT_SPEED;
  public FormationType currentFormation = DEFAULT_FORMATION;
  public float formationSpacing = DEFAULT_FORMATION_SPACING;
  public boolean formationChanged = DEFAULT_FORMATION_CHANGED;

  @Override
  public void reset() {
    movementSpeed = DEFAULT_MOVEMENT_SPEED;
    currentFormation = DEFAULT_FORMATION;
    formationSpacing = DEFAULT_FORMATION_SPACING;
    formationChanged = DEFAULT_FORMATION_CHANGED;
  }
}
