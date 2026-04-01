package io.github.proyectoM.systems.companion.movement;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** A system that coordinates the formation movement of all companions. */
public class SquadMovementSystem extends EntitySystem {

  private final ComponentMapper<GroupControllerComponent> controllerMapper =
      ComponentMapper.getFor(GroupControllerComponent.class);
  private final ComponentMapper<SquadMovementComponent> squadMovementMapper =
      ComponentMapper.getFor(SquadMovementComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<DeadComponent> deadMapper =
      ComponentMapper.getFor(DeadComponent.class);
  private final ComponentMapper<AttackingComponent> attackingMapper =
      ComponentMapper.getFor(AttackingComponent.class);

  private Entity squadControlEntity = null;
  private GroupControllerComponent controller = null;

  private final List<Entity> groupMembers = new ArrayList<>();
  private final Vector2 targetBuffer = new Vector2();

  private boolean formationNeedsRecalculation = false;

  private static final float DISTANCE_THRESHOLD = 5.0f;

  /**
   * Invoked when the system is removed from the Engine.
   *
   * @param engine The Ashley engine.
   */
  @Override
  public void removedFromEngine(Engine engine) {
    groupMembers.clear();
  }

  /**
   * Updates the system.
   *
   * @param deltaTime The time elapsed since the last frame.
   */
  @Override
  public void update(float deltaTime) {
    ImmutableArray<Entity> controllers =
        getEngine().getEntitiesFor(Family.all(GroupControllerComponent.class).get());
    if (controllers.size() > 0) {
      squadControlEntity = controllers.first();
      controller = controllerMapper.get(squadControlEntity);
    } else {
      squadControlEntity = null;
      controller = null;
    }

    discoverNewCompanions();

    if (formationNeedsRecalculation || (controller != null && controller.formationChanged)) {
      recalculateFormationOffsets();
      if (controller != null) {
        controller.formationChanged = false;
      }
      formationNeedsRecalculation = false;
    }

    moveTroops();
  }

  /**
   * Discovers new companions added to the engine and incorporates them into the group. If any are
   * added, it flags that the formation needs to be recalculated.
   */
  private void discoverNewCompanions() {
    ImmutableArray<Entity> allCompanions =
        getEngine()
            .getEntitiesFor(
                Family.all(CompanionComponent.class, SquadMovementComponent.class).get());
    if (allCompanions.size() > groupMembers.size()) {
      for (Entity companion : allCompanions) {
        if (!groupMembers.contains(companion)) {
          groupMembers.add(companion);
          formationNeedsRecalculation = true;
        }
      }
    }
  }

  /**
   * Moves ALL troops while maintaining their offsets relative to the virtual leader's position. The
   * virtual leader (GroupController) has already been moved by the InputSystem.
   */
  private void moveTroops() {
    if (groupMembers.isEmpty()) {
      return;
    }

    PositionComponent leaderPosition = positionMapper.get(squadControlEntity);

    Iterator<Entity> iterator = groupMembers.iterator();
    while (iterator.hasNext()) {
      Entity member = iterator.next();

      if (deadMapper.has(member)) {
        iterator.remove();
        formationNeedsRecalculation = true;
        continue;
      }

      PhysicsComponent physics = physicsMapper.get(member);

      if (attackingMapper.has(member)) {
        stopMember(physics);
        continue;
      }

      SquadMovementComponent memberComponent = squadMovementMapper.get(member);
      PositionComponent position = positionMapper.get(member);

      targetBuffer.set(
          leaderPosition.x + memberComponent.formationOffset.x,
          leaderPosition.y + memberComponent.formationOffset.y);

      moveToTarget(physics, position, targetBuffer);
    }
  }

  private void stopMember(PhysicsComponent physics) {
    if (physics != null && physics.body != null) {
      physics.body.setLinearVelocity(0, 0);
    }
  }

  /**
   * Moves an entity towards its target position using physics.
   *
   * @param physics The physics component containing the Box2D body.
   * @param position The current position component.
   * @param targetPosition The target position in pixels.
   */
  private void moveToTarget(
      PhysicsComponent physics, PositionComponent position, Vector2 targetPosition) {
    if (physics == null || physics.body == null) {
      return;
    }

    float deltaX = targetPosition.x - position.x;
    float deltaY = targetPosition.y - position.y;
    float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    if (distance > DISTANCE_THRESHOLD) {
      float velocityX = (deltaX / distance) * controller.movementSpeed;
      float velocityY = (deltaY / distance) * controller.movementSpeed;

      physics.body.setLinearVelocity(
          velocityX * io.github.proyectoM.physics.PhysicsConstants.METERS_PER_PIXEL,
          velocityY * io.github.proyectoM.physics.PhysicsConstants.METERS_PER_PIXEL);
    } else {
      physics.body.setLinearVelocity(0, 0);
    }
  }

  /** Recalculates formation offsets when the formation type changes. */
  private void recalculateFormationOffsets() {
    if (groupMembers.isEmpty()) {
      return;
    }
    FormationCalculator.recalculate(
        controller.currentFormation, controller.formationSpacing, groupMembers);
  }
}
