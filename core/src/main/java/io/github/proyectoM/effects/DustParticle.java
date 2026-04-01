package io.github.proyectoM.effects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/** Represents a single dust particle with simple physics and fading. */
public class DustParticle {
  private static final float MIN_SIZE = 2f;
  private static final float MAX_SIZE = 5f;
  private static final float MIN_SPEED = 10f;
  private static final float MAX_SPEED = 30f;
  private static final float MIN_LIFETIME = 0.3f;
  private static final float MAX_LIFETIME = 0.6f;
  private static final float MIN_ALPHA = 0.4f;
  private static final float MAX_ALPHA = 0.7f;
  private static final float UPWARD_VELOCITY = 15f;
  private static final float VERTICAL_SPEED_SCALE = 0.5f;
  private static final float VELOCITY_DAMPING_RATE = 2f;
  private static final float ALPHA_SCALE = 0.6f;

  public final Vector2 position;
  public final Vector2 velocity;
  public float lifetime;
  public float maxLifetime;
  public float size;
  public float alpha;

  public DustParticle(float x, float y) {
    position = new Vector2(x, y);
    size = MathUtils.random(MIN_SIZE, MAX_SIZE);
    maxLifetime = MathUtils.random(MIN_LIFETIME, MAX_LIFETIME);
    lifetime = maxLifetime;
    alpha = MathUtils.random(MIN_ALPHA, MAX_ALPHA);

    float angle = MathUtils.random(0f, MathUtils.PI2);
    float speed = MathUtils.random(MIN_SPEED, MAX_SPEED);
    velocity =
        new Vector2(
            MathUtils.cos(angle) * speed,
            MathUtils.sin(angle) * speed * VERTICAL_SPEED_SCALE + UPWARD_VELOCITY);
  }

  public boolean update(float deltaTime) {
    lifetime -= deltaTime;
    if (lifetime <= 0f) {
      return false;
    }

    position.x += velocity.x * deltaTime;
    position.y += velocity.y * deltaTime;
    velocity.scl(calculateDampingFactor(deltaTime));

    float lifeRatio = lifetime / maxLifetime;
    alpha = lifeRatio * ALPHA_SCALE;
    size *= lifeRatio;
    return true;
  }

  private float calculateDampingFactor(float deltaTime) {
    return 1f - deltaTime * VELOCITY_DAMPING_RATE;
  }
}
