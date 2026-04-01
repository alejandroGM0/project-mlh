package io.github.proyectoM.systems.rendering;

import box2dLight.ChainLight;
import box2dLight.ConeLight;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Creates and updates Box2D lights for entities with a {@link LightComponent}. */
public class LightSystem extends IteratingSystem {
  private static final float MIN_LIGHT_DISTANCE = 0.1f;
  private static final int CHAIN_LIGHT_RAY_DIRECTION = 1;

  private final RayHandler rayHandler;
  private final int defaultRays;
  private final Vector2 positionBuffer = new Vector2();
  private final ComponentMapper<LightComponent> lightMapper =
      ComponentMapper.getFor(LightComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);

  public LightSystem(RayHandler rayHandler, int defaultRays) {
    super(
        Family.all(LightComponent.class)
            .one(PhysicsComponent.class, PositionComponent.class)
            .get());
    this.rayHandler = rayHandler;
    this.defaultRays = defaultRays;
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    engine.addEntityListener(
        Family.all(LightComponent.class).one(PhysicsComponent.class, PositionComponent.class).get(),
        new EntityListener() {
          @Override
          public void entityAdded(Entity entity) {
            // No action needed on add; light is created lazily in processEntity.
          }

          @Override
          public void entityRemoved(Entity entity) {
            LightComponent light = lightMapper.get(entity);
            if (light != null && light.light != null) {
              light.light.remove();
              light.light = null;
            }
          }
        });
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    LightComponent light = lightMapper.get(entity);
    if (!light.active) {
      deactivateLight(light);
      return;
    }

    ensureLightInitialized(light);
    updateLightProperties(entity, light);
  }

  private void ensureLightInitialized(LightComponent light) {
    if (light.light != null) {
      return;
    }

    light.light = createLight(light);
    light.light.setSoftnessLength(light.softnessLength);
    light.light.setXray(light.xray);
  }

  private Light createLight(LightComponent light) {
    int rayCount = resolveRayCount(light);
    switch (light.type) {
      case CONE:
        return new ConeLight(
            rayHandler,
            rayCount,
            light.color,
            light.distance,
            light.positionMeters.x,
            light.positionMeters.y,
            light.coneDirectionDegrees,
            light.coneDegree);
      case CHAIN:
        if (light.chainVertices != null) {
          return new ChainLight(
              rayHandler,
              rayCount,
              light.color,
              light.distance,
              CHAIN_LIGHT_RAY_DIRECTION,
              light.chainVertices);
        }
        return new PointLight(
            rayHandler,
            rayCount,
            light.color,
            light.distance,
            light.positionMeters.x,
            light.positionMeters.y);
      case POINT:
      default:
        return new PointLight(
            rayHandler,
            rayCount,
            light.color,
            light.distance,
            light.positionMeters.x,
            light.positionMeters.y);
    }
  }

  private int resolveRayCount(LightComponent light) {
    return light.rays > 0 ? light.rays : defaultRays;
  }

  private void updateLightProperties(Entity entity, LightComponent light) {
    updateLightColor(light);
    updateLightDistance(light);
    updateLightPosition(entity, light);
    updateConeDirection(entity, light);
    light.light.setActive(true);
  }

  private void updateLightColor(LightComponent light) {
    light.light.setColor(light.color);
  }

  private void updateLightDistance(LightComponent light) {
    light.light.setDistance(Math.max(light.distance, MIN_LIGHT_DISTANCE));
  }

  private void updateLightPosition(Entity entity, LightComponent light) {
    Vector2 position = resolvePosition(entity, light);
    light.light.setPosition(position.x, position.y);
  }

  private Vector2 resolvePosition(Entity entity, LightComponent light) {
    PhysicsComponent physics = physicsMapper.get(entity);
    if (light.attachToPhysicsBody && physics != null && physics.body != null) {
      positionBuffer.set(physics.body.getPosition());
    } else if (light.useCustomPosition) {
      positionBuffer.set(light.positionMeters);
    } else {
      positionBuffer.set(resolveWorldPosition(entity, light));
    }

    positionBuffer.add(light.offsetMeters);
    return positionBuffer;
  }

  private Vector2 resolveWorldPosition(Entity entity, LightComponent light) {
    PositionComponent position = positionMapper.get(entity);
    if (position == null) {
      return positionBuffer.set(light.positionMeters);
    }

    return positionBuffer.set(
        position.x * PhysicsConstants.METERS_PER_PIXEL,
        position.y * PhysicsConstants.METERS_PER_PIXEL);
  }

  private void updateConeDirection(Entity entity, LightComponent light) {
    if (!(light.light instanceof ConeLight)) {
      return;
    }

    float direction = light.coneDirectionDegrees;
    PhysicsComponent physics = physicsMapper.get(entity);
    if (light.alignToBodyAngle && physics != null && physics.body != null) {
      direction = (float) Math.toDegrees(physics.body.getAngle());
    }

    light.light.setDirection(direction + light.directionOffsetDegrees);
  }

  private void deactivateLight(LightComponent light) {
    if (light.light != null) {
      light.light.setActive(false);
    }
  }
}
