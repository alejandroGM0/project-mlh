package io.github.proyectoM.systems.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import io.github.proyectoM.components.debug.AnimationControlComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent.MovementType;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.utils.DirectionUtils;

/** Updates movement direction state from velocity and facing angle. */
public class MovementDirectionStateSystem extends IteratingSystem {
  private static final float MINIMUM_VELOCITY_FOR_MOVEMENT = 0.01f;
  private static final float FORWARD_MOVEMENT_ANGLE_DIVISOR = 6f;
  private static final float BACKWARD_MOVEMENT_ANGLE_MULTIPLIER = 5f;
  private static final float FORWARD_MOVEMENT_ANGLE_THRESHOLD =
      MathUtils.PI / FORWARD_MOVEMENT_ANGLE_DIVISOR;
  private static final float BACKWARD_MOVEMENT_ANGLE_THRESHOLD =
      BACKWARD_MOVEMENT_ANGLE_MULTIPLIER * MathUtils.PI / FORWARD_MOVEMENT_ANGLE_DIVISOR;

  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<LookAtComponent> lookAtMapper =
      ComponentMapper.getFor(LookAtComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);
  private final ComponentMapper<AnimationControlComponent> controlMapper =
      ComponentMapper.getFor(AnimationControlComponent.class);

  public MovementDirectionStateSystem() {
    super(
        Family.all(
                PhysicsComponent.class,
                LookAtComponent.class,
                MovementDirectionStateComponent.class)
            .exclude(DeadComponent.class, AttackingComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PhysicsComponent physics = physicsMapper.get(entity);
    LookAtComponent lookAt = lookAtMapper.get(entity);
    MovementDirectionStateComponent movement = movementMapper.get(entity);

    if (controlMapper.has(entity)) {
      AnimationControlComponent control = controlMapper.get(entity);
      boolean overridden = false;

      if (control.lockMovement) {
        movement.movementType = control.forcedMovement;
        overridden = true;
      }
      if (control.lockDirection) {
        movement.directionIndex = control.forcedDirection;
        overridden = true;
      }

      if (overridden && control.lockMovement && control.lockDirection) {
        return;
      }
    }

    if (!shouldRecalculateMovement(physics, lookAt, movement)) {
      return;
    }

    updateCachedFaceAngle(movement, lookAt.angle);
    updateCachedVelocity(physics, movement);

    movement.isMoving = isEntityMoving(physics);

    processEntityMovement(physics, lookAt, movement);
  }

  private void updateCachedFaceAngle(
      MovementDirectionStateComponent movement, float currentFaceAngle) {
    movement.cachedFaceAngle = currentFaceAngle;
  }

  private void updateCachedVelocity(
      PhysicsComponent physics, MovementDirectionStateComponent movement) {
    float velocityX = physics.body.getLinearVelocity().x;
    float velocityY = physics.body.getLinearVelocity().y;
    movement.cachedVelocityMagnitude = getVelocityMagnitude(velocityX, velocityY);
  }

  private boolean shouldRecalculateMovement(
      PhysicsComponent physics, LookAtComponent lookAt, MovementDirectionStateComponent movement) {
    float velocityX = physics.body.getLinearVelocity().x;
    float velocityY = physics.body.getLinearVelocity().y;
    float currentFaceAngle = lookAt.angle;

    float currentVelocityMagnitude = getVelocityMagnitude(velocityX, velocityY);
    float currentVelocityAngle = MathUtils.atan2(velocityY, velocityX);

    boolean hasVelocityMagnitudeChanged =
        Math.abs(currentVelocityMagnitude - movement.cachedVelocityMagnitude)
            > MovementDirectionStateComponent.VELOCITY_THRESHOLD;
    boolean hasFaceAngleChanged =
        Math.abs(currentFaceAngle - movement.cachedFaceAngle)
            > MovementDirectionStateComponent.ANGLE_THRESHOLD;
    boolean hasVelocityAngleChanged =
        Math.abs(normalizeAngle(currentVelocityAngle - movement.cachedVelocityAngle))
            > MovementDirectionStateComponent.ANGLE_THRESHOLD;

    if (hasVelocityMagnitudeChanged || hasVelocityAngleChanged) {
      movement.cachedVelocityAngle = currentVelocityAngle;
    }

    return hasVelocityMagnitudeChanged || hasFaceAngleChanged || hasVelocityAngleChanged;
  }

  private boolean isEntityMoving(PhysicsComponent physics) {
    float velocityX = physics.body.getLinearVelocity().x;
    float velocityY = physics.body.getLinearVelocity().y;
    float velocityMagnitude = velocityX * velocityX + velocityY * velocityY;
    return velocityMagnitude >= MINIMUM_VELOCITY_FOR_MOVEMENT;
  }

  private void processEntityMovement(
      PhysicsComponent physics, LookAtComponent lookAt, MovementDirectionStateComponent movement) {
    if (movement.isMoving) {
      processMovingEntity(physics, lookAt, movement);
    } else {
      processIdleEntity(lookAt, movement);
    }
  }

  private void processMovingEntity(
      PhysicsComponent physics, LookAtComponent lookAt, MovementDirectionStateComponent movement) {
    float velocityX = physics.body.getLinearVelocity().x;
    float velocityY = physics.body.getLinearVelocity().y;

    movement.movementType = calculateMovementType(velocityX, velocityY, lookAt.angle);
    updateFacingDirection(lookAt, movement);
  }

  private void processIdleEntity(LookAtComponent lookAt, MovementDirectionStateComponent movement) {
    updateFacingDirection(lookAt, movement);
    movement.movementType = MovementType.FORWARD;
  }

  private MovementType calculateMovementType(float velocityX, float velocityY, float faceAngle) {
    float movementAngle = MathUtils.atan2(velocityY, velocityX);
    float faceAngleCartesian = faceAngle + DirectionUtils.ISOMETRIC_ANGLE_OFFSET_RADIANS;
    float angleDifference = normalizeAngle(movementAngle - faceAngleCartesian);
    float absoluteAngleDifference = Math.abs(angleDifference);

    if (absoluteAngleDifference <= FORWARD_MOVEMENT_ANGLE_THRESHOLD) {
      return MovementType.FORWARD;
    } else if (absoluteAngleDifference >= BACKWARD_MOVEMENT_ANGLE_THRESHOLD) {
      return MovementType.BACKWARDS;
    } else if (angleDifference > 0f) {
      return MovementType.STRAFE_LEFT;
    } else {
      return MovementType.STRAFE_RIGHT;
    }
  }

  private float normalizeAngle(float angle) {
    while (angle > MathUtils.PI) {
      angle -= MathUtils.PI2;
    }
    while (angle < -MathUtils.PI) {
      angle += MathUtils.PI2;
    }
    return angle;
  }

  private void updateFacingDirection(
      LookAtComponent lookAt, MovementDirectionStateComponent movement) {
    movement.directionIndex = calculateDirectionIndex(lookAt.angle);
    movement.faceAngle =
        DirectionUtils.snapToNearestDirection(
            (lookAt.angle + DirectionUtils.ISOMETRIC_ANGLE_OFFSET_RADIANS)
                * MathUtils.radiansToDegrees);
  }

  private float getVelocityMagnitude(float velocityX, float velocityY) {
    return (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
  }

  private int calculateDirectionIndex(float angleRad) {
    return DirectionUtils.calculateDirectionIndex(angleRad);
  }
}
