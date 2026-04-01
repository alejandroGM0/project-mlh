package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.proyectoM.components.entity.combat.PendingRemoveComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Removes bullets when they collide or exceed their travel distance. */
public class BulletLifeSystem extends IteratingSystem {
  private static final float DISTANCE_EPSILON = 0.0001f;

  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<BulletComponent> bulletMapper =
      ComponentMapper.getFor(BulletComponent.class);

  public BulletLifeSystem() {
    super(Family.all(PhysicsComponent.class, BulletComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PhysicsComponent physics = physicsMapper.get(entity);
    BulletComponent bullet = bulletMapper.get(entity);

    if (shouldRemoveBullet(entity)) {
      destroyBullet(entity, physics.body);
      return;
    }

    if (physics.body == null) {
      getEngine().removeEntity(entity);
      return;
    }

    float travelledDistance = calculateTravelledDistance(physics.body, bullet);
    bullet.distanceTravelled = travelledDistance;
    if (travelledDistance >= bullet.maxDistance - DISTANCE_EPSILON) {
      destroyBullet(entity, physics.body);
    }
  }

  private boolean shouldRemoveBullet(Entity entity) {
    return entity.getComponent(PendingRemoveComponent.class) != null;
  }

  private float calculateTravelledDistance(Body body, BulletComponent bullet) {
    float x = body.getPosition().x * PhysicsConstants.PIXELS_PER_METER;
    float y = body.getPosition().y * PhysicsConstants.PIXELS_PER_METER;
    float dx = x - bullet.startX;
    float dy = y - bullet.startY;
    return (float) Math.hypot(dx, dy);
  }

  private void destroyBullet(Entity entity, Body body) {
    if (body != null) {
      body.getWorld().destroyBody(body);
    }
    getEngine().removeEntity(entity);
  }
}
