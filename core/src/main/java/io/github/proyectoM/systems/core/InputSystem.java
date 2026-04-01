package io.github.proyectoM.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Reads keyboard input and moves the group controller body accordingly. */
public class InputSystem extends EntitySystem {
  private final ComponentMapper<GroupControllerComponent> controllerMapper =
      ComponentMapper.getFor(GroupControllerComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);

  @Override
  public void update(float deltaTime) {
    ImmutableArray<Entity> controllers =
        getEngine().getEntitiesFor(Family.all(GroupControllerComponent.class).get());
    if (controllers.size() == 0) {
      return;
    }

    Entity controller = controllers.first();
    GroupControllerComponent group = controllerMapper.get(controller);
    PhysicsComponent physics = physicsMapper.get(controller);
    if (physics == null || physics.body == null) {
      return;
    }

    float vx = 0f;
    float vy = 0f;

    if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
      vy += 1f;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      vy -= 1f;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      vx -= 1f;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
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
