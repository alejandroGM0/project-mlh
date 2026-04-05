package io.github.proyectoM.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.settings.GameSettings;

/** Reads keyboard input and moves the group controller body accordingly. */
public class InputSystem extends EntitySystem {
  private final ComponentMapper<GroupControllerComponent> controllerMapper =
      ComponentMapper.getFor(GroupControllerComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);

  private ImmutableArray<Entity> controllers;

  @Override
  public void addedToEngine(Engine engine) {
    controllers = engine.getEntitiesFor(
        Family.all(GroupControllerComponent.class, PhysicsComponent.class).get());
  }

  @Override
  public void update(float deltaTime) {
    if (controllers.size() == 0) {
      return;
    }

    Entity controller = controllers.first();
    GroupControllerComponent group = controllerMapper.get(controller);
    PhysicsComponent physics = physicsMapper.get(controller);

    float vx = 0f;
    float vy = 0f;

    GameSettings settings = GameSettings.getInstance();

    if (Gdx.input.isKeyPressed(settings.getMoveUpKey())) {
      vy += 1f;
    }
    if (Gdx.input.isKeyPressed(settings.getMoveDownKey())) {
      vy -= 1f;
    }
    if (Gdx.input.isKeyPressed(settings.getMoveLeftKey())) {
      vx -= 1f;
    }
    if (Gdx.input.isKeyPressed(settings.getMoveRightKey())) {
      vx += 1f;
    }

    float speed = group.movementSpeed * PhysicsConstants.METERS_PER_PIXEL;

    if (vx != 0f || vy != 0f) {
      float length = (float) Math.sqrt(vx * vx + vy * vy);
      vx = (vx / length) * speed;
      vy = (vy / length) * speed;
    }

    physics.body.setLinearVelocity(vx, vy);
  }
}
