package io.github.proyectoM.systems.animation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.proyectoM.components.entity.InventoryComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent.ActionType;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import java.util.Objects;

/** Derives the current action state for animatable entities. */
public class ActionStateSystem extends IteratingSystem {
  private final ComponentMapper<ActionStateComponent> actionStateMapper =
      ComponentMapper.getFor(ActionStateComponent.class);
  private final ComponentMapper<MovementDirectionStateComponent> movementMapper =
      ComponentMapper.getFor(MovementDirectionStateComponent.class);
  private final ComponentMapper<DeadComponent> deadMapper =
      ComponentMapper.getFor(DeadComponent.class);
  private final ComponentMapper<AttackingComponent> attackingMapper =
      ComponentMapper.getFor(AttackingComponent.class);
  private final ComponentMapper<InventoryComponent> inventoryMapper =
      ComponentMapper.getFor(InventoryComponent.class);

  public ActionStateSystem() {
    super(
        Family.all(
                ActionStateComponent.class,
                MovementDirectionStateComponent.class,
                InventoryComponent.class,
                AnimationComponent.class)
            .get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    ActionStateComponent actionState = actionStateMapper.get(entity);
    actionState.actionTime += deltaTime;

    if (deadMapper.has(entity)) {
      setActionIfDifferent(actionState, ActionType.DIE);
      return;
    }
    if (attackingMapper.has(entity)) {
      setActionIfDifferent(actionState, ActionType.ATTACK);
      return;
    }

    updateLivingEntityState(entity, actionState, movementMapper.get(entity));
  }

  private void updateLivingEntityState(
      Entity entity, ActionStateComponent actionState, MovementDirectionStateComponent movement) {
    if (movement.isMoving) {
      setActionIfDifferent(actionState, ActionType.MOVE);
      return;
    }
    if (isPrimaryWeaponAttacking(entity)) {
      setActionIfDifferent(actionState, ActionType.ATTACK);
      return;
    }

    setActionIfDifferent(actionState, ActionType.IDLE);
  }

  private void setActionIfDifferent(ActionStateComponent actionState, ActionType newType) {
    if (actionState.actionType == newType) {
      return;
    }

    actionState.actionType = newType;
    actionState.actionTime = 0f;
  }

  private boolean isPrimaryWeaponAttacking(Entity entity) {
    InventoryComponent inventory = inventoryMapper.get(entity);
    if (inventory.weapons.size == 0) {
      return false;
    }

    Entity primaryWeapon = inventory.weapons.first();
    WeaponComponent weaponComponent =
        Objects.requireNonNull(
            primaryWeapon.getComponent(WeaponComponent.class),
            "ActionStateSystem expected the primary inventory entity to contain WeaponComponent.");
    return weaponComponent.isAttacking;
  }
}
