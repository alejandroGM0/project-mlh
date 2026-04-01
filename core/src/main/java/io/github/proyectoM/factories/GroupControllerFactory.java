package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/**
 * Creates the virtual group controller entity with a sensor physics body and a point light that
 * acts as the squad's flashlight.
 */
public final class GroupControllerFactory {
  private static final float CONTROLLER_RADIUS_PIXELS = 8f;
  private static final float CONTROLLER_BODY_DENSITY = 1f;
  private static final float CONTROLLER_BODY_FRICTION = 0.2f;
  private static final float CONTROLLER_BODY_RESTITUTION = 0f;
  private static final float CONTROLLER_BODY_LINEAR_DAMPING = 4f;
  private static final boolean CONTROLLER_FIXTURE_IS_SENSOR = true;
  private static final BodyDef.BodyType CONTROLLER_BODY_TYPE = BodyDef.BodyType.DynamicBody;

  private static final float FLASHLIGHT_DISTANCE = 22f;
  private static final int FLASHLIGHT_RAY_COUNT = 128;
  private static final float FLASHLIGHT_SOFTNESS = 2.5f;
  private static final Color FLASHLIGHT_COLOR = new Color(1.0f, 0.95f, 0.8f, 0.9f);

  private GroupControllerFactory() {}

  /**
   * Creates the group controller entity with physics, position, and flashlight.
   *
   * @param engine ECS engine to register the entity in
   * @param world Box2D world for the physics body
   * @param initialX initial horizontal position in pixels
   * @param initialY initial vertical position in pixels
   * @return the created group controller entity
   */
  public static Entity createGroupController(
      Engine engine, World world, float initialX, float initialY) {
    Entity controller = engine.createEntity();
    controller.add(engine.createComponent(GroupControllerComponent.class));
    PositionComponent pos = engine.createComponent(PositionComponent.class);
    pos.x = initialX;
    pos.y = initialY;
    controller.add(pos);
    controller.add(createPhysicsComponent(engine, world, controller, initialX, initialY));
    controller.add(createFlashlightComponent(engine));
    engine.addEntity(controller);
    return controller;
  }

  private static PhysicsComponent createPhysicsComponent(
      Engine engine, World world, Entity controller, float x, float y) {
    Body body = createControllerBody(world, controller, x, y);
    attachControllerFixture(body);

    PhysicsComponent physics = engine.createComponent(PhysicsComponent.class);
    physics.body = body;
    return physics;
  }

  private static Body createControllerBody(World world, Entity controller, float x, float y) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = CONTROLLER_BODY_TYPE;
    bodyDef.position.set(
        x * PhysicsConstants.METERS_PER_PIXEL, y * PhysicsConstants.METERS_PER_PIXEL);

    Body body = world.createBody(bodyDef);
    body.setLinearDamping(CONTROLLER_BODY_LINEAR_DAMPING);
    body.setUserData(controller);
    return body;
  }

  private static void attachControllerFixture(Body body) {
    CircleShape shape = new CircleShape();
    shape.setRadius(CONTROLLER_RADIUS_PIXELS * PhysicsConstants.METERS_PER_PIXEL);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = CONTROLLER_BODY_DENSITY;
    fixtureDef.friction = CONTROLLER_BODY_FRICTION;
    fixtureDef.restitution = CONTROLLER_BODY_RESTITUTION;
    fixtureDef.isSensor = CONTROLLER_FIXTURE_IS_SENSOR;

    body.createFixture(fixtureDef);
    shape.dispose();
  }

  /**
   * Creates a circular point-light component that acts as the player's flashlight.
   *
   * @param engine ECS engine for pooling
   * @return light component configured as a POINT flashlight
   */
  private static LightComponent createFlashlightComponent(Engine engine) {
    LightComponent light = engine.createComponent(LightComponent.class);
    light.type = LightComponent.LightType.POINT;
    light.color.set(FLASHLIGHT_COLOR);
    light.distance = FLASHLIGHT_DISTANCE;
    light.rays = FLASHLIGHT_RAY_COUNT;
    light.softnessLength = FLASHLIGHT_SOFTNESS;
    light.xray = false;
    light.attachToPhysicsBody = true;
    light.active = true;
    return light;
  }
}
