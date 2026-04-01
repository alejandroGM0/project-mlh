package io.github.proyectoM.systems.physics;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.World;

/**
 * A system responsible for advancing the Box2D physics simulation in each frame. It must be
 * executed before synchronization and rendering systems.
 */
public class PhysicsSystem extends EntitySystem {
  private static final float TIME_STEP = 1 / 60f;
  private static final int VELOCITY_ITERATIONS = 4;
  private static final int POSITION_ITERATIONS = 1;

  private final World world;
  private float accumulator = 0f;

  /**
   * Constructor for the physics system. It receives the Box2D physics world to simulate.
   *
   * @param world The Box2D World instance.
   */
  public PhysicsSystem(World world) {
    this.world = world;
  }

  /**
   * {@inheritDoc} Advances the physics simulation by accumulating time and performing fixed steps.
   *
   * @param deltaTime The time elapsed since the last frame.
   */
  @Override
  public void update(float deltaTime) {
    accumulator += deltaTime;
    while (accumulator >= TIME_STEP) {
      world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
      accumulator -= TIME_STEP;
    }
  }
}
