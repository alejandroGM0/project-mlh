package io.github.proyectoM.systems.enemy.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.movement.SteeringComponent;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.utils.SpatialHash;
import java.util.ArrayList;
import java.util.List;

/** A system that applies separation forces between enemies to prevent crowding. */
public class EnemySeparationSystem extends EntitySystem {
  private static final float DEFAULT_ENTITY_RADIUS_PIXELS = 32f;
  private static final float SPATIAL_HASH_CELL_SIZE = 150f;
  private static final float MIN_SEPARATION_DISTANCE = 50f;
  private static final float SEPARATION_FORCE_MULTIPLIER = 0.5f;
  private static final float MIN_DISTANCE_EPSILON = 0.1f;
  private static final float RANDOM_DIRECTION_OFFSET = 0.5f;
  private static final float RANDOM_DIRECTION_SCALE = 2f;
  private static final float MIN_RANDOM_DISTANCE = 1f;
  private static final float MIN_FORCE = 0f;
  private static final float MAX_FORCE = 1f;

  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<SteeringComponent> steeringMapper =
      ComponentMapper.getFor(SteeringComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);

  private ImmutableArray<Entity> enemies;
  private final SpatialHash spatialHash;
  private final List<Entity> nearbyEntities = new ArrayList<>();
  private final Vector2 separationForce = new Vector2();
  private final Vector2 awayVector = new Vector2();

  public EnemySeparationSystem() {
    this.spatialHash = new SpatialHash(SPATIAL_HASH_CELL_SIZE);
  }

  @Override
  public void addedToEngine(Engine engine) {
    enemies =
        engine.getEntitiesFor(
            Family.all(
                    EnemyComponent.class,
                    PositionComponent.class,
                    SteeringComponent.class,
                    PhysicsComponent.class)
                .get());
  }

  @Override
  public void update(float deltaTime) {
    if (enemies.size() == 0) {
      return;
    }

    spatialHash.clear();

    for (int i = 0; i < enemies.size(); i++) {
      Entity enemy = enemies.get(i);
      PositionComponent position = positionMapper.get(enemy);
      spatialHash.insert(enemy, position.x, position.y);
    }

    for (int i = 0; i < enemies.size(); i++) {
      Entity enemy = enemies.get(i);
      applySeparationForce(enemy);
    }
  }

  private void applySeparationForce(Entity entity) {
    PositionComponent position = positionMapper.get(entity);
    SteeringComponent steering = steeringMapper.get(entity);
    PhysicsComponent physics = physicsMapper.get(entity);

    if (!steering.separationEnabled) {
      return;
    }

    steering.clearForces();

    spatialHash.queryRadius(position.x, position.y, steering.separationRadius, nearbyEntities);

    separationForce.setZero();
    int neighborCount = 0;

    float entityRadius = getEntityRadius(physics);

    for (Entity neighbor : nearbyEntities) {
      if (neighbor == entity) {
        continue;
      }

      PositionComponent neighborPos = positionMapper.get(neighbor);
      PhysicsComponent neighborPhysics = physicsMapper.get(neighbor);

      awayVector.set(position.x - neighborPos.x, position.y - neighborPos.y);
      float distance = awayVector.len();

      if (distance < MIN_DISTANCE_EPSILON) {
        assignRandomSeparationDirection();
        distance = MIN_RANDOM_DISTANCE;
      }

      float neighborRadius = getEntityRadius(neighborPhysics);
      float minDistance = entityRadius + neighborRadius + MIN_SEPARATION_DISTANCE;

      if (distance < minDistance) {
        awayVector.nor();

        float strength = MAX_FORCE - (distance / minDistance);
        strength = Math.max(MIN_FORCE, Math.min(MAX_FORCE, strength));

        awayVector.scl(strength);
        separationForce.add(awayVector);
        neighborCount++;
      }
    }

    if (neighborCount > 0) {
      separationForce.scl(steering.separationStrength * SEPARATION_FORCE_MULTIPLIER);

      if (separationForce.len() > steering.maxSeparationForce) {
        separationForce.nor().scl(steering.maxSeparationForce);
      }

      steering.separationForce.set(separationForce);
    }
  }

  private void assignRandomSeparationDirection() {
    awayVector.set(
        (MathUtils.random() - RANDOM_DIRECTION_OFFSET) * RANDOM_DIRECTION_SCALE,
        (MathUtils.random() - RANDOM_DIRECTION_OFFSET) * RANDOM_DIRECTION_SCALE);
  }

  private float getEntityRadius(PhysicsComponent physics) {
    if (physics == null || physics.body == null) {
      return DEFAULT_ENTITY_RADIUS_PIXELS;
    }

    Fixture fixture = physics.body.getFixtureList().first();
    if (fixture != null && fixture.getShape() instanceof CircleShape) {
      CircleShape circle = (CircleShape) fixture.getShape();
      return circle.getRadius() / PhysicsConstants.METERS_PER_PIXEL;
    }

    return DEFAULT_ENTITY_RADIUS_PIXELS;
  }
}
