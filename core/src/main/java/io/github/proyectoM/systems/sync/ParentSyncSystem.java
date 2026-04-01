/**
 * ParentSyncSystem.java
 *
 * <p>An ECS system that synchronizes data from parent entities to child entities. Processes
 * entities with ParentComponent and copies relevant data: - PositionComponent (x, y) -
 * MovementDirectionStateComponent (if present) - ActionStateComponent (if present)
 *
 * <p>This system must run AFTER movement/state systems but BEFORE AnimationSelectionSystem and
 * RenderSystem.
 *
 * <p>Project: ProjectM Author: AlejandroGM0 Date: 2025-12-12
 */
package io.github.proyectoM.systems.sync;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;

/** A system that synchronizes position and state data from parent to child entities. */
public class ParentSyncSystem extends IteratingSystem {

  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);
  private final ComponentMapper<ActionStateComponent> actionMapper =
      ComponentMapper.getFor(ActionStateComponent.class);

  /**
   * Constructor for ParentSyncSystem. Processes all entities with ParentComponent and
   * PositionComponent.
   */
  public ParentSyncSystem() {
    super(Family.all(ParentComponent.class, PositionComponent.class).get());
  }

  /**
   * Synchronizes the child entity's data with its parent.
   *
   * @param entity The child entity to process.
   * @param deltaTime The time elapsed since the last frame.
   */
  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    ParentComponent parentComp = parentMapper.get(entity);
    Entity parent = parentComp.parent;

    if (parent == null) {
      return;
    }

    syncPosition(entity, parent);
    syncMovementDirection(entity, parent);
    syncActionState(entity, parent);
  }

  /** Copies position from parent to child. */
  private void syncPosition(Entity child, Entity parent) {
    PositionComponent parentPos = positionMapper.get(parent);
    PositionComponent childPos = positionMapper.get(child);

    if (parentPos != null && childPos != null) {
      childPos.x = parentPos.x;
      childPos.y = parentPos.y;
    }
  }

  /** Copies movement direction state from parent to child if both have the component. */
  private void syncMovementDirection(Entity child, Entity parent) {
    MovementDirectionStateComponent parentMovement = movementMapper.get(parent);
    MovementDirectionStateComponent childMovement = movementMapper.get(child);

    if (parentMovement != null && childMovement != null) {
      childMovement.isMoving = parentMovement.isMoving;
      childMovement.movementType = parentMovement.movementType;
      childMovement.directionIndex = parentMovement.directionIndex;
      childMovement.faceAngle = parentMovement.faceAngle;
    }
  }

  /** Copies action state from parent to child if both have the component. */
  private void syncActionState(Entity child, Entity parent) {
    ActionStateComponent parentAction = actionMapper.get(parent);
    ActionStateComponent childAction = actionMapper.get(child);

    if (parentAction != null && childAction != null) {
      childAction.actionType = parentAction.actionType;
      childAction.actionTime = parentAction.actionTime;
    }
  }
}
