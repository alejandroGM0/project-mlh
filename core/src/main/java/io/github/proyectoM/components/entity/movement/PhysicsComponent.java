package io.github.proyectoM.components.entity.movement;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Holds the Box2D body associated with an entity. */
public class PhysicsComponent implements Component, Poolable {
  public Body body;

  @Override
  public void reset() {
    body = null;
  }
}
