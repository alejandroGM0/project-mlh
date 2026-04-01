package io.github.proyectoM.systems.physics;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/**
 * Synchronizes the position of a Box2D Body with the entity's PositionComponent after the physics
 * step.
 */
public class PhysicsSyncSystem extends EntitySystem {
  private static final float INTERPOLATION_SPEED = 15f;

  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);

  private ImmutableArray<Entity> entities;

  @Override
  public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
    entities =
        engine.getEntitiesFor(Family.all(PhysicsComponent.class, PositionComponent.class).get());
  }

  @Override
  public void update(float deltaTime) {
    float alpha = Math.min(1f, INTERPOLATION_SPEED * deltaTime);

    for (int i = 0; i < entities.size(); ++i) {
      Entity entity = entities.get(i);
      PhysicsComponent physics = physicsMapper.get(entity);
      PositionComponent position = positionMapper.get(entity);
      if (physics.body != null) {
        Vector2 bodyPos = physics.body.getPosition();
        float targetX = bodyPos.x * PhysicsConstants.PIXELS_PER_METER;
        float targetY = bodyPos.y * PhysicsConstants.PIXELS_PER_METER;

        position.x = MathUtils.lerp(position.x, targetX, alpha);
        position.y = MathUtils.lerp(position.y, targetY, alpha);
      }
    }
  }
}
