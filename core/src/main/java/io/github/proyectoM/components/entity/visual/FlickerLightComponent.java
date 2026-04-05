package io.github.proyectoM.components.entity.visual;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Adds a flickering or pulsing distance effect to a light. */
public class FlickerLightComponent implements Component, Poolable {
  public static final float DEFAULT_BASE_DISTANCE = 0f;
  public static final float DEFAULT_AMOUNT = 0.15f;
  public static final float DEFAULT_SPEED = 5f;
  public static final float DEFAULT_TIMER = 0f;
  public static final boolean DEFAULT_FLICKERING = true;

  public float baseDistance = DEFAULT_BASE_DISTANCE;
  public float amount = DEFAULT_AMOUNT;
  public float speed = DEFAULT_SPEED;
  public float timer = DEFAULT_TIMER;
  public boolean isFlickering = DEFAULT_FLICKERING;

  @Override
  public void reset() {
    baseDistance = DEFAULT_BASE_DISTANCE;
    amount = DEFAULT_AMOUNT;
    speed = DEFAULT_SPEED;
    timer = DEFAULT_TIMER;
    isFlickering = DEFAULT_FLICKERING;
  }
}
