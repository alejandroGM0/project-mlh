package io.github.proyectoM.components.entity.movement;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Stores steering configuration and accumulated steering forces. */
public class SteeringComponent implements Component, Poolable {
  public static final float DEFAULT_SEPARATION_RADIUS = 100f;
  public static final float DEFAULT_SEPARATION_STRENGTH = 3f;
  public static final float DEFAULT_MAX_SEPARATION_FORCE = 200f;

  public boolean separationEnabled = true;
  public float separationRadius = DEFAULT_SEPARATION_RADIUS;
  public float separationStrength = DEFAULT_SEPARATION_STRENGTH;
  public float maxSeparationForce = DEFAULT_MAX_SEPARATION_FORCE;
  public final Vector2 separationForce = new Vector2();

  public SteeringComponent() {}

  public SteeringComponent(float separationRadius, float separationStrength) {
    this.separationRadius = separationRadius;
    this.separationStrength = separationStrength;
  }

  public void clearForces() {
    separationForce.setZero();
  }

  @Override
  public void reset() {
    separationEnabled = true;
    separationRadius = DEFAULT_SEPARATION_RADIUS;
    separationStrength = DEFAULT_SEPARATION_STRENGTH;
    maxSeparationForce = DEFAULT_MAX_SEPARATION_FORCE;
    separationForce.setZero();
  }
}
