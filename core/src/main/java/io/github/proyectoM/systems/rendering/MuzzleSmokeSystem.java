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
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.weapon.MuzzleFlashComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.effects.MuzzleSmokeParticle;
import io.github.proyectoM.systems.combat.weapons.MuzzleFlashSystem;

/** Renders smoke and spark particles when a weapon flash is triggered. */
public class MuzzleSmokeSystem extends IteratingSystem {
  private static final int MAX_PARTICLES = 150;
  private static final int SMOKE_PARTICLES_PER_SHOT = 3;
  private static final int SPARK_PARTICLES_PER_SHOT = 5;
  private static final float SMOKE_ANGLE_FALLBACK_DEGREES = 0f;
  private static final float RADIANS_TO_DEGREES = MathUtils.radiansToDegrees;
  private static final float SPAWN_THRESHOLD_SECONDS = 0.02f;
  private static final float SMOKE_RED = 0.5f;
  private static final float SMOKE_GREEN = 0.5f;
  private static final float SMOKE_BLUE = 0.5f;
  private static final float SPARK_RED = 1f;
  private static final float SPARK_GREEN = 0.8f;
  private static final float SPARK_BLUE = 0.3f;
  private static final float FULL_ALPHA = 1f;
  private static final Color SMOKE_COLOR =
      new Color(SMOKE_RED, SMOKE_GREEN, SMOKE_BLUE, FULL_ALPHA);
  private static final Color SPARK_COLOR =
      new Color(SPARK_RED, SPARK_GREEN, SPARK_BLUE, FULL_ALPHA);

  private final ShapeRenderer shapeRenderer;
  private final OrthographicCamera camera;
  private final Array<MuzzleSmokeParticle> particles = new Array<>(MAX_PARTICLES);
  private final ComponentMapper<MuzzleFlashComponent> flashMapper =
      ComponentMapper.getFor(MuzzleFlashComponent.class);
  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<WeaponStateComponent> weaponStateMapper =
      ComponentMapper.getFor(WeaponStateComponent.class);
  private final ComponentMapper<MuzzlePointComponent> muzzleMapper =
      ComponentMapper.getFor(MuzzlePointComponent.class);
  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<LookAtComponent> lookAtMapper =
      ComponentMapper.getFor(LookAtComponent.class);

  public MuzzleSmokeSystem(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
    super(Family.all(MuzzleFlashComponent.class).get());
    this.shapeRenderer = shapeRenderer;
    this.camera = camera;
  }

  @Override
  public void update(float deltaTime) {
    super.update(deltaTime);
    updateParticles(deltaTime);
    renderParticles();
  }

  @Override
  protected void processEntity(Entity flashEntity, float deltaTime) {
    MuzzleFlashComponent flash = flashMapper.get(flashEntity);
    Entity weaponEntity = flash != null ? flash.weaponEntity : null;
    if (weaponEntity == null || !canSpawnParticles(weaponEntity)) {
      return;
    }

    Entity companionEntity = getParentEntity(weaponEntity);
    MuzzlePointComponent muzzle = muzzleMapper.get(companionEntity);
    if (companionEntity == null || muzzle == null) {
      return;
    }

    float angleDegrees = resolveAngleDegrees(companionEntity);
    spawnSmokeParticles(muzzle, angleDegrees);
    spawnSparkParticles(muzzle, angleDegrees);
  }

  private boolean canSpawnParticles(Entity weaponEntity) {
    WeaponStateComponent weaponState = weaponStateMapper.get(weaponEntity);
    if (weaponState == null || isParticlePoolFull()) {
      return false;
    }

    float flashDuration = MuzzleFlashSystem.FLASH_DURATION;
    return weaponState.flashTimer > 0f && weaponState.flashTimer >= flashDuration - SPAWN_THRESHOLD_SECONDS;
  }

  private boolean isParticlePoolFull() {
    int particlesPerShot = SMOKE_PARTICLES_PER_SHOT + SPARK_PARTICLES_PER_SHOT;
    return particles.size > MAX_PARTICLES - particlesPerShot;
  }

  private Entity getParentEntity(Entity childEntity) {
    ParentComponent parent = parentMapper.get(childEntity);
    return parent != null ? parent.parent : null;
  }

  private float resolveAngleDegrees(Entity companionEntity) {
    LookAtComponent lookAt = lookAtMapper.get(companionEntity);
    if (lookAt == null) {
      return SMOKE_ANGLE_FALLBACK_DEGREES;
    }

    return lookAt.angle * RADIANS_TO_DEGREES;
  }

  private void spawnSmokeParticles(MuzzlePointComponent muzzle, float angleDegrees) {
    for (int i = 0; i < SMOKE_PARTICLES_PER_SHOT; i++) {
      particles.add(
          new MuzzleSmokeParticle(muzzle.position.x, muzzle.position.y, angleDegrees, false));
    }
  }

  private void spawnSparkParticles(MuzzlePointComponent muzzle, float angleDegrees) {
    for (int i = 0; i < SPARK_PARTICLES_PER_SHOT; i++) {
      particles.add(
          new MuzzleSmokeParticle(muzzle.position.x, muzzle.position.y, angleDegrees, true));
    }
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

    for (MuzzleSmokeParticle particle : particles) {
      applyParticleColor(particle);
      shapeRenderer.circle(particle.position.x, particle.position.y, particle.size);
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void enableBlendMode() {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  private void applyParticleColor(MuzzleSmokeParticle particle) {
    Color color = particle.isSpark ? SPARK_COLOR : SMOKE_COLOR;
    shapeRenderer.setColor(color.r, color.g, color.b, particle.alpha);
  }
}
