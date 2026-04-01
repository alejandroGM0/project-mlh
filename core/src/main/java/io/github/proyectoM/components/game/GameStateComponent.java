package io.github.proyectoM.components.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores the global game state on the shared gameplay state entity. */
public class GameStateComponent implements Component, Poolable {
  private static final State RESET_STATE = State.RUNNING;

  public enum State {
    RUNNING,
    GAME_OVER
  }

  public State currentState = RESET_STATE;

  @Override
  public void reset() {
    currentState = RESET_STATE;
  }
}
