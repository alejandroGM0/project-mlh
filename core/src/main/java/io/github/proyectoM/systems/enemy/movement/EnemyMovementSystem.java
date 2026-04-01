package io.github.proyectoM.systems.enemy.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.AIComponent;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.combat.TargetComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.movement.SteeringComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** A system that moves enemies directly towards their targets with steering behaviors. */
public class EnemyMovementSystem extends IteratingSystem {
  private static final float NO_MOVEMENT_DAMPING = 0f;
  private static final float STOPPING_DAMPING = 20f;
  private static final float STOP_DISTANCE_BUFFER = 30f;
  private static final float NO_RADIUS_PIXELS = 0f;

  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<TargetComponent> targetMapper =
      ComponentMapper.getFor(TargetComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<AIComponent> aiComponentMapper =
      ComponentMapper.getFor(AIComponent.class);
  private final ComponentMapper<SteeringComponent> steeringMapper =
      ComponentMapper.getFor(SteeringComponent.class);

  private final Vector2 tempVector = new Vector2();
  private final Vector2 steeringDirection = new Vector2();

  public EnemyMovementSystem() {
    super(
        Family.all(
                EnemyComponent.class,
                PositionComponent.class,
                TargetComponent.class,
                AIComponent.class,
                PhysicsComponent.class)
            .exclude(DeadComponent.class, AttackingComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PhysicsComponent physics = physicsMapper.get(entity);
    PositionComponent position = positionMapper.get(entity);
    TargetComponent target = targetMapper.get(entity);
    AIComponent aiComponent = aiComponentMapper.get(entity);
    SteeringComponent steering = steeringMapper.get(entity);

    if (target.targetEntity == null) {
      stopMovement(physics);
      return;
    }

    PositionComponent targetPosition = positionMapper.get(target.targetEntity);

    PhysicsComponent targetPhysics = physicsMapper.get(target.targetEntity);
    if (isCloseEnoughToTarget(position, targetPosition, physics, targetPhysics)) {
      stopMovement(physics);
      return;
    }

    tempVector.set(targetPosition.x - position.x, targetPosition.y - position.y);
    if (tempVector.isZero()) {
      stopMovement(physics);
      return;
    }

    tempVector.nor();

    Vector2 finalMovementDirection = calculateMovementDirection(tempVector, steering);

    applyMovementForce(physics, finalMovementDirection, aiComponent.speed);
  }

  /**
   * Checks if the enemy is close enough to the target to stop. Calculates the distance based on the
   * radii of the physical bodies.
   *
   * @param position The enemy's position.
   * @param targetPosition The target's position.
   * @param enemyPhysics The enemy's physics component.
   * @param targetPhysics The target's physics component.
   * @return true if the enemy should stop, false if it should continue moving.
   */
  private boolean isCloseEnoughToTarget(
      PositionComponent position,
      PositionComponent targetPosition,
      PhysicsComponent enemyPhysics,
      PhysicsComponent targetPhysics) {
    tempVector.set(targetPosition.x - position.x, targetPosition.y - position.y);
    float distanceToTarget = tempVector.len();

    float enemyRadius = getBodyRadius(enemyPhysics);
    float targetRadius = getBodyRadius(targetPhysics);
    float stopDistance = (enemyRadius + targetRadius + STOP_DISTANCE_BUFFER);

    return distanceToTarget <= stopDistance;
  }

  /**
   * Gets the radius of the physical body in pixels.
   *
   * @param physics The physics component.
   * @return The radius of the body in pixels.
   */
  private float getBodyRadius(PhysicsComponent physics) {
    if (physics == null || physics.body == null) {
      return NO_RADIUS_PIXELS;
    }

    Fixture fixture = physics.body.getFixtureList().first();
    if (fixture != null && fixture.getShape() instanceof CircleShape) {
      CircleShape circle = (CircleShape) fixture.getShape();
      return circle.getRadius() / PhysicsConstants.METERS_PER_PIXEL;
    }

    return NO_RADIUS_PIXELS;
  }

  /**
   * Calculates the final movement direction by combining the path with steering.
   *
   * @param baseDirection The normalized direction towards the waypoint.
   * @param steering The steering component associated with the entity.
   * @return The adjusted normalized direction.
   */
  private Vector2 calculateMovementDirection(Vector2 baseDirection, SteeringComponent steering) {
    steeringDirection.set(baseDirection);

    if (steering != null && steering.separationEnabled && !steering.separationForce.isZero()) {
      steeringDirection.add(steering.separationForce);
    }

    if (!steeringDirection.isZero()) {
      steeringDirection.nor();
    } else {
      steeringDirection.set(baseDirection);
    }

    return steeringDirection;
  }

  /**
   * Applies a movement force to the physical body.
   *
   * @param physics The physics component.
   * @param direction The normalized direction of movement.
   * @param speed The movement speed.
   */
  private void applyMovementForce(PhysicsComponent physics, Vector2 direction, float speed) {
    Body body = physics.body;

    float velocityX = direction.x * speed;
    float velocityY = direction.y * speed;

    body.setLinearDamping(NO_MOVEMENT_DAMPING);
    body.setLinearVelocity(velocityX, velocityY);
  }

  /**
   * Stops the enemy's movement.
   *
   * @param physics The physics component.
   */
  private void stopMovement(PhysicsComponent physics) {
    Body body = physics.body;
    body.setLinearVelocity(NO_MOVEMENT_DAMPING, NO_MOVEMENT_DAMPING);
    body.setLinearDamping(STOPPING_DAMPING);
  }
}
