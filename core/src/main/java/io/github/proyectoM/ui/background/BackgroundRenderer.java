package io.github.proyectoM.ui.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Renders the animated menu background layers and particles. */
public class BackgroundRenderer implements Disposable {
  private static final float VIEWPORT_ORIGIN = 0f;
  private static final float FULL_ALPHA = 1f;
  private static final int RISING_PARTICLES = 60;
  private static final int AMBIENT_PARTICLES = 40;
  private static final float GRID_SIZE = 32f;
  private static final float GRID_LINE_WIDTH = 1f;
  private static final float GRID_SCROLL_SPEED = 15f;

  private static final float RISING_PARTICLE_MIN_SIZE = 1f;
  private static final float RISING_PARTICLE_MAX_SIZE = 3f;
  private static final float RISING_PARTICLE_MIN_SPEED = 20f;
  private static final float RISING_PARTICLE_MAX_SPEED = 50f;
  private static final float RISING_PARTICLE_SPAWN_Y_MIN = -50f;
  private static final float RISING_PARTICLE_SPAWN_Y_MAX = 0f;

  private static final float AMBIENT_PARTICLE_MIN_SIZE = 1.5f;
  private static final float AMBIENT_PARTICLE_MAX_SIZE = 4f;
  private static final float AMBIENT_PARTICLE_MIN_SPEED = 5f;
  private static final float AMBIENT_PARTICLE_MAX_SPEED = 15f;

  private static final float PARTICLE_RECYCLE_OFFSET = 10f;
  private static final float PARTICLE_OFFSCREEN_THRESHOLD = 50f;

  private static final int GRADIENT_STEPS = 50;
  private static final int BLUR_LAYERS = 3;
  private static final float BLUR_ALPHA_BASE = 0.02f;
  private static final float BLUR_OFFSET_MULTIPLIER = 2f;
  private static final int BLUR_GRID_SIZE = 16;
  private static final float BLUR_NOISE_FACTOR = 0.03f;

  private static final int VIGNETTE_STEPS = 20;
  private static final float VIGNETTE_STRENGTH = 0.25f;
  private static final float VIGNETTE_THICKNESS_FACTOR = 0.15f;

  private static final float RISING_PARTICLE_WIDTH_RATIO = 0.8f;
  private static final float RISING_PARTICLE_HEIGHT_RATIO = 0.5f;
  private static final float RISING_PARTICLE_WIDTH_SIZE_MULTIPLIER = 1.6f;
  private static final int RISING_PARTICLE_ELLIPSE_SEGMENTS = 16;
  private static final float RISING_PARTICLE_CORE_SIZE_RATIO = 0.3f;
  private static final float RISING_PARTICLE_CORE_ALPHA_MULTIPLIER = 0.7f;

  private static final float AMBIENT_PARTICLE_HALF_SIZE_RATIO = 0.5f;

  private static final float BG_TOP_R = 0.08f;
  private static final float BG_TOP_G = 0.12f;
  private static final float BG_TOP_B = 0.08f;
  private static final float BG_TOP_A = 1f;

  private static final float BG_BOTTOM_R = 0.02f;
  private static final float BG_BOTTOM_G = 0.03f;
  private static final float BG_BOTTOM_B = 0.02f;
  private static final float BG_BOTTOM_A = 1f;

  private static final float GRID_R = 0.15f;
  private static final float GRID_G = 0.2f;
  private static final float GRID_B = 0.15f;
  private static final float GRID_A = 0.3f;

  private static final float RISING_R = 0.4f;
  private static final float RISING_G = 0.8f;
  private static final float RISING_B = 0.6f;
  private static final float RISING_A = 0.5f;

  private static final float AMBIENT_R = 0.3f;
  private static final float AMBIENT_G = 0.4f;
  private static final float AMBIENT_B = 0.3f;
  private static final float AMBIENT_A = 0.2f;

  private static final float WHITE_R = 1f;
  private static final float WHITE_G = 1f;
  private static final float WHITE_B = 1f;

  private static final float RISING_X_SWAY_FREQUENCY = 2f;
  private static final float AMBIENT_VELOCITY_VARIATION = 0.5f;
  private static final float AMBIENT_VELOCITY_DRIFT_MULTIPLIER = 2f;
  private static final float AMBIENT_MAX_SPEED_MULTIPLIER = 1.5f;
  private static final float AMBIENT_PARTICLE_ALPHA_PULSE_SPEED = 1.5f;
  private static final float AMBIENT_ALPHA_BASE = 0.3f;
  private static final float AMBIENT_ALPHA_RANGE = 0.3f;
  private static final float BLUR_LAYER_ALPHA_FALLOFF = 0.3f;
  private static final int BLUR_HASH_X_MULTIPLIER = 31;
  private static final int BLUR_HASH_Y_MULTIPLIER = 17;
  private static final int BLUR_HASH_LAYER_MULTIPLIER = 23;
  private static final int BLUR_HASH_MODULUS = 100;
  private static final float BLUR_HASH_CENTER_OFFSET = 0.5f;
  private static final float RISING_PARTICLE_INITIAL_ALPHA_MIN = 0.4f;
  private static final float RISING_PARTICLE_INITIAL_ALPHA_MAX = 0.9f;
  private static final float RISING_PARTICLE_REFRESH_ALPHA_MIN = 0.6f;
  private static final float RISING_PARTICLE_REFRESH_ALPHA_MAX = 0.9f;
  private static final float RISING_PARTICLE_SWAY_AMPLITUDE_MIN = 10f;
  private static final float RISING_PARTICLE_SWAY_AMPLITUDE_MAX = 30f;
  private static final float AMBIENT_PARTICLE_INITIAL_ALPHA_MIN = 0.3f;
  private static final float AMBIENT_PARTICLE_INITIAL_ALPHA_MAX = 0.7f;

  private final ShapeRenderer shapeRenderer;
  private final List<RisingParticle> risingParticles;
  private final List<AmbientParticle> ambientParticles;
  private final OrthographicCamera camera;

  private float time = 0f;
  private float gridOffsetY = 0f;
  private float viewportWidth;
  private float viewportHeight;

  private final Color bgColorTop = new Color(BG_TOP_R, BG_TOP_G, BG_TOP_B, BG_TOP_A);
  private final Color bgColorBottom = new Color(BG_BOTTOM_R, BG_BOTTOM_G, BG_BOTTOM_B, BG_BOTTOM_A);
  private final Color gridColor = new Color(GRID_R, GRID_G, GRID_B, GRID_A);
  private final Color risingColor = new Color(RISING_R, RISING_G, RISING_B, RISING_A);
  private final Color ambientColor = new Color(AMBIENT_R, AMBIENT_G, AMBIENT_B, AMBIENT_A);

  /**
   * Creates a background renderer for the given camera and viewport.
   *
   * @param camera camera used for projection
   * @param width viewport width
   * @param height viewport height
   */
  public BackgroundRenderer(OrthographicCamera camera, float width, float height) {
    this.camera = Objects.requireNonNull(camera, "camera");
    this.viewportWidth = width;
    this.viewportHeight = height;

    this.shapeRenderer = new ShapeRenderer();
    this.risingParticles = new ArrayList<>();
    this.ambientParticles = new ArrayList<>();

    initializeParticles();
  }

  private void initializeParticles() {
    for (int i = 0; i < RISING_PARTICLES; i++) {
      risingParticles.add(createRisingParticle());
    }

    for (int i = 0; i < AMBIENT_PARTICLES; i++) {
      ambientParticles.add(createAmbientParticle());
    }
  }

  /**
   * Updates particle animation state.
   *
   * @param delta seconds since the previous frame
   */
  public void update(float delta) {
    time += delta;
    updateGridOffset(delta);

    for (RisingParticle particle : risingParticles) {
      particle.update(delta, time);
      recycleRisingParticle(particle);
    }

    for (AmbientParticle particle : ambientParticles) {
      particle.update(delta, time);
      recycleAmbientParticle(particle);
    }
  }

  private RisingParticle createRisingParticle() {
    return new RisingParticle(
        MathUtils.random(VIEWPORT_ORIGIN, viewportWidth),
        MathUtils.random(RISING_PARTICLE_SPAWN_Y_MIN, RISING_PARTICLE_SPAWN_Y_MAX),
        MathUtils.random(RISING_PARTICLE_MIN_SIZE, RISING_PARTICLE_MAX_SIZE),
        MathUtils.random(RISING_PARTICLE_MIN_SPEED, RISING_PARTICLE_MAX_SPEED));
  }

  private AmbientParticle createAmbientParticle() {
    return new AmbientParticle(
        MathUtils.random(VIEWPORT_ORIGIN, viewportWidth),
        MathUtils.random(VIEWPORT_ORIGIN, viewportHeight),
        MathUtils.random(AMBIENT_PARTICLE_MIN_SIZE, AMBIENT_PARTICLE_MAX_SIZE),
        MathUtils.random(AMBIENT_PARTICLE_MIN_SPEED, AMBIENT_PARTICLE_MAX_SPEED));
  }

  private void updateGridOffset(float delta) {
    gridOffsetY += GRID_SCROLL_SPEED * delta;
    if (gridOffsetY >= GRID_SIZE) {
      gridOffsetY -= GRID_SIZE;
    }
  }

  private void recycleRisingParticle(RisingParticle particle) {
    if (particle.position.y <= viewportHeight + PARTICLE_OFFSCREEN_THRESHOLD) {
      return;
    }

    particle.position.y = -PARTICLE_RECYCLE_OFFSET;
    particle.position.x = MathUtils.random(VIEWPORT_ORIGIN, viewportWidth);
  }

  private void recycleAmbientParticle(AmbientParticle particle) {
    particle.position.x = wrapParticleCoordinate(particle.position.x, viewportWidth);
    particle.position.y = wrapParticleCoordinate(particle.position.y, viewportHeight);
  }

  private float wrapParticleCoordinate(float coordinate, float viewportLimit) {
    if (coordinate < -PARTICLE_RECYCLE_OFFSET) {
      return viewportLimit + PARTICLE_RECYCLE_OFFSET;
    }
    if (coordinate > viewportLimit + PARTICLE_RECYCLE_OFFSET) {
      return -PARTICLE_RECYCLE_OFFSET;
    }
    return coordinate;
  }

  /** Renders the full background stack. */
  public void render() {
    shapeRenderer.setProjectionMatrix(camera.combined);

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    renderGradient();
    renderGrid();
    renderAmbientParticles();
    renderRisingParticles();
    renderBlurEffect();
    renderVignette();

    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void renderGradient() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (int i = 0; i < GRADIENT_STEPS; i++) {
      float normalizedY = i / (float) GRADIENT_STEPS;
      float y = normalizedY * viewportHeight;
      float height = viewportHeight / GRADIENT_STEPS;

      float red = MathUtils.lerp(bgColorBottom.r, bgColorTop.r, normalizedY);
      float green = MathUtils.lerp(bgColorBottom.g, bgColorTop.g, normalizedY);
      float blue = MathUtils.lerp(bgColorBottom.b, bgColorTop.b, normalizedY);

      shapeRenderer.setColor(red, green, blue, FULL_ALPHA);
      shapeRenderer.rect(VIEWPORT_ORIGIN, y, viewportWidth, height);
    }

    shapeRenderer.end();
  }

  private void renderBlurEffect() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (int layer = 0; layer < BLUR_LAYERS; layer++) {
      float alpha = BLUR_ALPHA_BASE * (FULL_ALPHA - layer * BLUR_LAYER_ALPHA_FALLOFF);
      float offset = layer * BLUR_OFFSET_MULTIPLIER;

      for (int x = 0; x < viewportWidth; x += BLUR_GRID_SIZE) {
        for (int y = 0; y < viewportHeight; y += BLUR_GRID_SIZE) {
          float hash =
              ((x * BLUR_HASH_X_MULTIPLIER
                          + y * BLUR_HASH_Y_MULTIPLIER
                          + layer * BLUR_HASH_LAYER_MULTIPLIER)
                      % BLUR_HASH_MODULUS)
                  / (float) BLUR_HASH_MODULUS;
          float noise = (hash - BLUR_HASH_CENTER_OFFSET) * BLUR_NOISE_FACTOR;

          shapeRenderer.setColor(
              bgColorBottom.r + noise, bgColorBottom.g + noise, bgColorBottom.b + noise, alpha);

          shapeRenderer.rect(x + offset, y + offset, BLUR_GRID_SIZE, BLUR_GRID_SIZE);
        }
      }
    }

    shapeRenderer.end();
  }

  private void renderGrid() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(gridColor);

    for (float x = 0; x <= viewportWidth; x += GRID_SIZE) {
      shapeRenderer.rect(x, VIEWPORT_ORIGIN, GRID_LINE_WIDTH, viewportHeight);
    }

    for (float y = -gridOffsetY; y <= viewportHeight; y += GRID_SIZE) {
      shapeRenderer.rect(VIEWPORT_ORIGIN, y, viewportWidth, GRID_LINE_WIDTH);
    }

    shapeRenderer.end();
  }

  private void renderAmbientParticles() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (AmbientParticle particle : ambientParticles) {
      renderAmbientParticle(particle);
    }

    shapeRenderer.end();
  }

  private void renderRisingParticles() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (RisingParticle particle : risingParticles) {
      renderRisingParticle(particle);
    }

    shapeRenderer.end();
  }

  private void renderAmbientParticle(AmbientParticle particle) {
    float alpha = ambientColor.a * particle.alpha;
    float halfSize = particle.size * AMBIENT_PARTICLE_HALF_SIZE_RATIO;

    shapeRenderer.setColor(ambientColor.r, ambientColor.g, ambientColor.b, alpha);
    shapeRenderer.triangle(
        particle.position.x,
        particle.position.y + halfSize,
        particle.position.x - halfSize,
        particle.position.y,
        particle.position.x + halfSize,
        particle.position.y);
    shapeRenderer.triangle(
        particle.position.x,
        particle.position.y - halfSize,
        particle.position.x - halfSize,
        particle.position.y,
        particle.position.x + halfSize,
        particle.position.y);
  }

  private void renderRisingParticle(RisingParticle particle) {
    float alpha = risingColor.a * particle.alpha;
    float width = particle.size * RISING_PARTICLE_WIDTH_SIZE_MULTIPLIER;
    float coreAlpha = alpha * RISING_PARTICLE_CORE_ALPHA_MULTIPLIER;
    float coreRadius = particle.size * RISING_PARTICLE_CORE_SIZE_RATIO;

    shapeRenderer.setColor(risingColor.r, risingColor.g, risingColor.b, alpha);
    shapeRenderer.ellipse(
        particle.position.x - particle.size * RISING_PARTICLE_WIDTH_RATIO,
        particle.position.y - particle.size * RISING_PARTICLE_HEIGHT_RATIO,
        width,
        particle.size,
        RISING_PARTICLE_ELLIPSE_SEGMENTS);

    shapeRenderer.setColor(WHITE_R, WHITE_G, WHITE_B, coreAlpha);
    shapeRenderer.circle(
        particle.position.x, particle.position.y, coreRadius, RISING_PARTICLE_ELLIPSE_SEGMENTS);
  }

  private void renderVignette() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (int i = 0; i < VIGNETTE_STEPS; i++) {
      float progress = i / (float) VIGNETTE_STEPS;
      float alpha = progress * VIGNETTE_STRENGTH;
      float thickness = viewportWidth * VIGNETTE_THICKNESS_FACTOR * (1 - progress);

      shapeRenderer.setColor(VIEWPORT_ORIGIN, VIEWPORT_ORIGIN, VIEWPORT_ORIGIN, alpha);
      shapeRenderer.rect(VIEWPORT_ORIGIN, VIEWPORT_ORIGIN, thickness, viewportHeight);
      shapeRenderer.rect(viewportWidth - thickness, VIEWPORT_ORIGIN, thickness, viewportHeight);
      shapeRenderer.rect(VIEWPORT_ORIGIN, VIEWPORT_ORIGIN, viewportWidth, thickness);
      shapeRenderer.rect(VIEWPORT_ORIGIN, viewportHeight - thickness, viewportWidth, thickness);
    }

    shapeRenderer.end();
  }

  /**
   * Rebuilds particles for a new viewport size.
   *
   * @param width new viewport width
   * @param height new viewport height
   */
  public void resize(float width, float height) {
    this.viewportWidth = width;
    this.viewportHeight = height;

    risingParticles.clear();
    ambientParticles.clear();
    initializeParticles();
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
  }

  private static final class RisingParticle {
    private final Vector2 position;
    private final float size;
    private final float speed;
    private float alpha;
    private final float swayPhase;
    private final float swayAmplitude;

    private RisingParticle(float x, float y, float size, float speed) {
      this.position = new Vector2(x, y);
      this.size = size;
      this.speed = speed;
      this.alpha =
          MathUtils.random(RISING_PARTICLE_INITIAL_ALPHA_MIN, RISING_PARTICLE_INITIAL_ALPHA_MAX);
      this.swayPhase = MathUtils.random(VIEWPORT_ORIGIN, MathUtils.PI2);
      this.swayAmplitude =
          MathUtils.random(RISING_PARTICLE_SWAY_AMPLITUDE_MIN, RISING_PARTICLE_SWAY_AMPLITUDE_MAX);
    }

    private void update(float delta, float elapsedTime) {
      position.y += speed * delta;
      position.x +=
          MathUtils.sin(elapsedTime * RISING_X_SWAY_FREQUENCY + swayPhase) * swayAmplitude * delta;
      alpha =
          MathUtils.random(RISING_PARTICLE_REFRESH_ALPHA_MIN, RISING_PARTICLE_REFRESH_ALPHA_MAX);
    }
  }

  private static final class AmbientParticle {
    private final Vector2 position;
    private final Vector2 velocity;
    private final float size;
    private final float speed;
    private float alpha;
    private final float alphaPhase;

    private AmbientParticle(float x, float y, float size, float speed) {
      this.position = new Vector2(x, y);
      this.size = size;
      this.speed = speed;
      this.alpha =
          MathUtils.random(AMBIENT_PARTICLE_INITIAL_ALPHA_MIN, AMBIENT_PARTICLE_INITIAL_ALPHA_MAX);
      this.alphaPhase = MathUtils.random(VIEWPORT_ORIGIN, MathUtils.PI2);

      float angle = MathUtils.random(VIEWPORT_ORIGIN, MathUtils.PI2);
      this.velocity = new Vector2(MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed);
    }

    private void update(float delta, float elapsedTime) {
      position.x += velocity.x * delta;
      position.y += velocity.y * delta;

      float drift = MathUtils.sin(elapsedTime + alphaPhase) * AMBIENT_VELOCITY_VARIATION;
      velocity.x += drift * delta * AMBIENT_VELOCITY_DRIFT_MULTIPLIER;
      velocity.y +=
          MathUtils.cos(elapsedTime + alphaPhase)
              * AMBIENT_VELOCITY_VARIATION
              * delta
              * AMBIENT_VELOCITY_DRIFT_MULTIPLIER;

      if (velocity.len() > speed * AMBIENT_MAX_SPEED_MULTIPLIER) {
        velocity.nor().scl(speed);
      }

      alpha =
          AMBIENT_ALPHA_BASE
              + MathUtils.sin(elapsedTime * AMBIENT_PARTICLE_ALPHA_PULSE_SPEED + alphaPhase)
                  * AMBIENT_ALPHA_RANGE;
    }
  }
}
