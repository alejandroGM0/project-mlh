package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.components.visual.SpriteComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Steers homing bullets toward their target position. */
public class BulletHomingSystem extends IteratingSystem {
  private static final float MIN_ROTATION_VELOCITY_SQUARED = 0.001f;
  private static final float TARGET_REACHED_EPSILON_SQUARED = 0.0001f;

  private final ComponentMapper<BulletComponent> bulletMapper =
      ComponentMapper.getFor(BulletComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<SpriteComponent> spriteMapper =
      ComponentMapper.getFor(SpriteComponent.class);

  private final Vector2 currentVelocity = new Vector2();
  private final Vector2 desiredVelocity = new Vector2();
  private final Vector2 steering = new Vector2();
  private final Vector2 bulletPosition = new Vector2();

  public BulletHomingSystem() {
    super(Family.all(BulletComponent.class, PhysicsComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    BulletComponent bullet = bulletMapper.get(entity);
    if (!bullet.isHoming) {
      return;
    }

    PhysicsComponent physics = physicsMapper.get(entity);
    if (physics.body == null) {
      return;
    }

    steerTowardsTarget(bullet, physics, deltaTime);
    updateSpriteRotation(entity, physics);
  }

  private void steerTowardsTarget(
      BulletComponent bullet, PhysicsComponent physics, float deltaTime) {
    bulletPosition.set(
        physics.body.getPosition().x * PhysicsConstants.PIXELS_PER_METER,
        physics.body.getPosition().y * PhysicsConstants.PIXELS_PER_METER);

    currentVelocity.set(physics.body.getLinearVelocity());
    float speedInMeters = bullet.speed * PhysicsConstants.METERS_PER_PIXEL;

    desiredVelocity.set(bullet.targetX, bullet.targetY).sub(bulletPosition);
    if (desiredVelocity.len2() <= TARGET_REACHED_EPSILON_SQUARED) {
      return;
    }

    desiredVelocity.nor().scl(speedInMeters);
    steering.set(desiredVelocity).sub(currentVelocity).scl(bullet.homingStrength * deltaTime);
    currentVelocity.add(steering);
    if (currentVelocity.isZero()) {
      return;
    }

    currentVelocity.nor().scl(speedInMeters);
    physics.body.setLinearVelocity(currentVelocity);
  }

  private void updateSpriteRotation(Entity entity, PhysicsComponent physics) {
    SpriteComponent sprite = spriteMapper.get(entity);
    if (sprite == null) {
      return;
    }

    Vector2 velocity = physics.body.getLinearVelocity();
    if (velocity.len2() > MIN_ROTATION_VELOCITY_SQUARED) {
      sprite.angle = velocity.angleDeg();
    }
  }
}
