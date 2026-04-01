package io.github.proyectoM.effects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/** Represents a single smoke or spark particle emitted from a muzzle flash. */
public class MuzzleSmokeParticle {
  private static final float SMOKE_MIN_SIZE = 4f;
  private static final float SMOKE_MAX_SIZE = 8f;
  private static final float SPARK_MIN_SIZE = 1f;
  private static final float SPARK_MAX_SIZE = 3f;
  private static final float SPARK_MIN_LIFETIME = 0.1f;
  private static final float SPARK_MAX_LIFETIME = 0.2f;
  private static final float SMOKE_MIN_LIFETIME = 0.2f;
  private static final float SMOKE_MAX_LIFETIME = 0.4f;
  private static final float SPARK_MIN_ALPHA = 0.8f;
  private static final float SPARK_MAX_ALPHA = 1f;
  private static final float SMOKE_MIN_ALPHA = 0.3f;
  private static final float SMOKE_MAX_ALPHA = 0.5f;
  private static final float SPARK_SPREAD_MIN_DEGREES = -30f;
  private static final float SPARK_SPREAD_MAX_DEGREES = 30f;
  private static final float SMOKE_SPREAD_MIN_DEGREES = -45f;
  private static final float SMOKE_SPREAD_MAX_DEGREES = 45f;
  private static final float SPARK_MIN_SPEED = 100f;
  private static final float SPARK_MAX_SPEED = 200f;
  private static final float SMOKE_MIN_SPEED = 20f;
  private static final float SMOKE_MAX_SPEED = 50f;
  private static final float SMOKE_UPWARD_VELOCITY = 20f;
  private static final float VELOCITY_DAMPING_RATE = 3f;
  private static final float SMOKE_ALPHA_SCALE = 0.4f;
  private static final float SMOKE_GROWTH_RATE = 10f;

  public final Vector2 position;
  public final Vector2 velocity;
  public float lifetime;
  public float maxLifetime;
  public float size;
  public float alpha;
  public boolean isSpark;

  public MuzzleSmokeParticle(float x, float y, float angle, boolean isSpark) {
    position = new Vector2(x, y);
    this.isSpark = isSpark;
    velocity = createVelocity(angle, isSpark);
    configureParticleState(isSpark);
    lifetime = maxLifetime;
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
    if (isSpark) {
      alpha = lifeRatio;
    } else {
      alpha = lifeRatio * SMOKE_ALPHA_SCALE;
      size += deltaTime * SMOKE_GROWTH_RATE;
    }

    return true;
  }

  private void configureParticleState(boolean sparkParticle) {
    if (sparkParticle) {
      size = MathUtils.random(SPARK_MIN_SIZE, SPARK_MAX_SIZE);
      maxLifetime = MathUtils.random(SPARK_MIN_LIFETIME, SPARK_MAX_LIFETIME);
      alpha = MathUtils.random(SPARK_MIN_ALPHA, SPARK_MAX_ALPHA);
      return;
    }

    size = MathUtils.random(SMOKE_MIN_SIZE, SMOKE_MAX_SIZE);
    maxLifetime = MathUtils.random(SMOKE_MIN_LIFETIME, SMOKE_MAX_LIFETIME);
    alpha = MathUtils.random(SMOKE_MIN_ALPHA, SMOKE_MAX_ALPHA);
  }

  private Vector2 createVelocity(float angle, boolean sparkParticle) {
    if (sparkParticle) {
      float spreadAngle =
          angle + MathUtils.random(SPARK_SPREAD_MIN_DEGREES, SPARK_SPREAD_MAX_DEGREES);
      float speed = MathUtils.random(SPARK_MIN_SPEED, SPARK_MAX_SPEED);
      return new Vector2(
          MathUtils.cosDeg(spreadAngle) * speed, MathUtils.sinDeg(spreadAngle) * speed);
    }

    float spreadAngle =
        angle + MathUtils.random(SMOKE_SPREAD_MIN_DEGREES, SMOKE_SPREAD_MAX_DEGREES);
    float speed = MathUtils.random(SMOKE_MIN_SPEED, SMOKE_MAX_SPEED);
    return new Vector2(
        MathUtils.cosDeg(spreadAngle) * speed,
        MathUtils.sinDeg(spreadAngle) * speed + SMOKE_UPWARD_VELOCITY);
  }

  private float calculateDampingFactor(float deltaTime) {
    return 1f - deltaTime * VELOCITY_DAMPING_RATE;
  }
}
