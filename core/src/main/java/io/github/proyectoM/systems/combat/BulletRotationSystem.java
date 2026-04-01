package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.components.visual.SpriteComponent;

/** Smoothly rotates bullet sprites toward their trajectory angle. */
public class BulletRotationSystem extends IteratingSystem {
  private static final float ROTATION_COMPLETE_THRESHOLD = 1f;
  private static final float HALF_CIRCLE_DEGREES = 180f;
  private static final float FULL_CIRCLE_DEGREES = 360f;

  private final ComponentMapper<BulletComponent> bulletMapper =
      ComponentMapper.getFor(BulletComponent.class);
  private final ComponentMapper<SpriteComponent> spriteMapper =
      ComponentMapper.getFor(SpriteComponent.class);

  public BulletRotationSystem() {
    super(Family.all(BulletComponent.class, SpriteComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    BulletComponent bullet = bulletMapper.get(entity);
    if (!bullet.isRotating) {
      return;
    }

    SpriteComponent sprite = spriteMapper.get(entity);
    float angleDifference = calculateShortestAngleDifference(sprite.angle, bullet.targetRotation);
    if (Math.abs(angleDifference) < ROTATION_COMPLETE_THRESHOLD) {
      sprite.angle = bullet.targetRotation;
      bullet.isRotating = false;
      return;
    }

    float maxRotation = bullet.rotationSpeed * deltaTime;
    float rotationStep =
        Math.abs(angleDifference) <= maxRotation
            ? angleDifference
            : Math.signum(angleDifference) * maxRotation;
    sprite.angle += rotationStep;
  }

  private float calculateShortestAngleDifference(float currentAngle, float targetAngle) {
    float difference = targetAngle - currentAngle;
    while (difference < -HALF_CIRCLE_DEGREES) {
      difference += FULL_CIRCLE_DEGREES;
    }
    while (difference > HALF_CIRCLE_DEGREES) {
      difference -= FULL_CIRCLE_DEGREES;
    }
    return difference;
  }
}
