package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.effects.DustParticle;

/** Renders dust particles under moving entities. */
public class DustParticleSystem extends IteratingSystem {
  private static final float SPAWN_INTERVAL_SECONDS = 0.08f;
  private static final int MAX_PARTICLES = 200;
  private static final float OFFSET_X_MIN = -10f;
  private static final float OFFSET_X_MAX = 10f;
  private static final float OFFSET_Y_MIN = -20f;
  private static final float OFFSET_Y_MAX = -10f;
  private static final float DUST_RED = 0.6f;
  private static final float DUST_GREEN = 0.55f;
  private static final float DUST_BLUE = 0.45f;
  private static final float FULL_ALPHA = 1f;
  private static final Color DUST_COLOR = new Color(DUST_RED, DUST_GREEN, DUST_BLUE, FULL_ALPHA);

  private final ShapeRenderer shapeRenderer;
  private final OrthographicCamera camera;
  private final Array<DustParticle> particles = new Array<>(MAX_PARTICLES);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);

  private float spawnTimer = 0f;

  public DustParticleSystem(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
    super(Family.all(PositionComponent.class, MovementDirectionStateComponent.class).get());
    this.shapeRenderer = shapeRenderer;
    this.camera = camera;
  }

  @Override
  public void update(float deltaTime) {
    spawnTimer += deltaTime;
    super.update(deltaTime);
    updateParticles(deltaTime);
    renderParticles();
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    MovementDirectionStateComponent movement = movementMapper.get(entity);
    PositionComponent position = positionMapper.get(entity);
    if (!movement.isMoving) {
      return;
    }

    if (spawnTimer < SPAWN_INTERVAL_SECONDS || particles.size >= MAX_PARTICLES) {
      return;
    }

    particles.add(new DustParticle(position.x + randomOffsetX(), position.y + randomOffsetY()));
    spawnTimer = 0f;
  }

  private float randomOffsetX() {
    return MathUtils.random(OFFSET_X_MIN, OFFSET_X_MAX);
  }

  private float randomOffsetY() {
    return MathUtils.random(OFFSET_Y_MIN, OFFSET_Y_MAX);
  }

  private void updateParticles(float deltaTime) {
    for (int i = particles.size - 1; i >= 0; i--) {
      if (!particles.get(i).update(deltaTime)) {
        particles.removeIndex(i);
      }
    }
  }

  private void renderParticles() {
    if (particles.isEmpty()) {
      return;
    }

    enableBlendMode();
    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    for (DustParticle particle : particles) {
      shapeRenderer.setColor(DUST_COLOR.r, DUST_COLOR.g, DUST_COLOR.b, particle.alpha);
      shapeRenderer.circle(particle.position.x, particle.position.y, particle.size);
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void enableBlendMode() {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }
}
