package io.github.proyectoM.systems.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.InventoryComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.systems.animation.ActionStateSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for ActionStateSystem.
 */
class ActionStateSystemTest {

    /**
     * Verifies that dead state takes priority over other state sources.
     */
    @Test
    void deadStateTakesPriorityOverOtherStateSources() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new ActionStateSystem());
        Entity entity = this.createAnimatedEntity();
        entity.add((Component)new DeadComponent());
        entity.add((Component)new AttackingComponent());
        engine.addEntity(entity);
        engine.update(0.1f);
        ActionStateComponent actionState = (ActionStateComponent)entity.getComponent(ActionStateComponent.class);
        Assertions.assertEquals(ActionStateComponent.ActionType.DIE, actionState.actionType);
    }

    /**
     * Verifies that weapon attack sets attack state when the entity is not moving.
     */
    @Test
    void weaponAttackSetsAttackStateWhenTheEntityIsNotMoving() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new ActionStateSystem());
        Entity entity = this.createAnimatedEntity();
        InventoryComponent inventory = (InventoryComponent)entity.getComponent(InventoryComponent.class);
        inventory.addWeapon(this.createWeaponEntity(true));
        engine.addEntity(entity);
        engine.update(0.1f);
        ActionStateComponent actionState = (ActionStateComponent)entity.getComponent(ActionStateComponent.class);
        Assertions.assertEquals(ActionStateComponent.ActionType.ATTACK, actionState.actionType);
    }

    /**
     * Verifies that moving entity uses move state when no higher priority state exists.
     */
    @Test
    void movingEntityUsesMoveStateWhenNoHigherPriorityStateExists() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new ActionStateSystem());
        Entity entity = this.createAnimatedEntity();
        ((MovementDirectionStateComponent)entity.getComponent(MovementDirectionStateComponent.class)).isMoving = true;
        engine.addEntity(entity);
        engine.update(0.1f);
        ActionStateComponent actionState = (ActionStateComponent)entity.getComponent(ActionStateComponent.class);
        Assertions.assertEquals(ActionStateComponent.ActionType.MOVE, actionState.actionType);
    }

    /**
     * Verifies that unchanged idle state keeps accumulated action time.
     */
    @Test
    void unchangedIdleStateKeepsAccumulatedActionTime() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new ActionStateSystem());
        Entity entity = this.createAnimatedEntity();
        ActionStateComponent actionState = (ActionStateComponent)entity.getComponent(ActionStateComponent.class);
        actionState.actionType = ActionStateComponent.ActionType.IDLE;
        actionState.actionTime = 0.5f;
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertEquals(0.6f, actionState.actionTime, 1.0E-4f);
    }

    /**
     * Verifies that invalid primary weapon fails fast.
     */
    @Test
    void invalidPrimaryWeaponFailsFast() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new ActionStateSystem());
        Entity entity = this.createAnimatedEntity();
        ((InventoryComponent)entity.getComponent(InventoryComponent.class)).addWeapon(new Entity());
        engine.addEntity(entity);
        Assertions.assertThrows(NullPointerException.class, () -> engine.update(0.1f));
    }

    private Entity createAnimatedEntity() {
        Entity entity = new Entity();
        entity.add((Component)new ActionStateComponent());
        entity.add((Component)new MovementDirectionStateComponent());
        entity.add((Component)new InventoryComponent());
        entity.add((Component)new AnimationComponent());
        return entity;
    }

    private Entity createWeaponEntity(boolean isAttacking) {
        WeaponComponent weaponComponent = new WeaponComponent();
        weaponComponent.id = "test-weapon";
        WeaponStateComponent weaponState = new WeaponStateComponent();
        weaponState.isAttacking = isAttacking;
        Entity weaponEntity = new Entity();
        weaponEntity.add((Component)weaponComponent);
        weaponEntity.add((Component)weaponState);
        return weaponEntity;
    }
}