package io.github.proyectoM.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;

/** Provides access to the shared Box2D world used by the game. */
public final class PhysicsWorldProvider {
  private static final Vector2 ZERO_GRAVITY = new Vector2();
  private static final boolean ALLOW_SLEEPING_BODIES = true;

  private static World world;

  private PhysicsWorldProvider() {}

  public static World getWorld() {
    if (world == null) {
      Box2D.init();
      world = new World(ZERO_GRAVITY, ALLOW_SLEEPING_BODIES);
    }
    return world;
  }

  public static void dispose() {
    if (world != null) {
      world.dispose();
      world = null;
    }
  }

  public static World resetWorld() {
    dispose();
    return getWorld();
  }
}
