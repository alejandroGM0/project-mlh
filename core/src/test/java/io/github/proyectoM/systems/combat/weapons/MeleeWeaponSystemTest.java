package io.github.proyectoM.systems.combat.weapons;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.AnimEventComponent;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.combat.PendingDamageComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.components.entity.weapon.types.MeleeWeaponComponent;
import io.github.proyectoM.systems.combat.weapons.MeleeWeaponSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for MeleeWeaponSystem.
 */
class MeleeWeaponSystemTest {

    /**
     * Verifies that in range target starts an attack and defines hit event.
     */
    @Test
    void inRangeTargetStartsAnAttackAndDefinesHitEvent() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new MeleeWeaponSystem());
        Entity owner = this.createOwner(0.0f, 0.0f);
        Entity target = this.createTarget(10.0f, 0.0f);
        Entity weapon = this.createMeleeWeapon(owner, target, 25.0f, 5);
        engine.addEntity(owner);
        engine.addEntity(target);
        engine.addEntity(weapon);
        engine.update(0.1f);
        Assertions.assertTrue(((WeaponStateComponent)weapon.getComponent(WeaponStateComponent.class)).isAttacking);
        Assertions.assertNotNull(owner.getComponent(AttackingComponent.class));
        AnimEventComponent events = (AnimEventComponent)owner.getComponent(AnimEventComponent.class);
        Assertions.assertNotNull(events);
        Assertions.assertTrue(events.eventFrames.containsKey(AnimEventComponent.AnimEventType.HIT_FRAME));
    }

    /**
     * Verifies that hit frame applies damage only once and end clears attack state.
     */
    @Test
    void hitFrameAppliesDamageOnlyOnceAndEndClearsAttackState() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new MeleeWeaponSystem());
        Entity owner = this.createOwner(0.0f, 0.0f);
        Entity target = this.createTarget(10.0f, 0.0f);
        Entity weapon = this.createMeleeWeapon(owner, target, 25.0f, 7);
        engine.addEntity(owner);
        engine.addEntity(target);
        engine.addEntity(weapon);
        engine.update(0.1f);
        AnimEventComponent events = (AnimEventComponent)owner.getComponent(AnimEventComponent.class);
        events.triggeredEvents.add(AnimEventComponent.AnimEventType.HIT_FRAME);
        engine.update(0.1f);
        Assertions.assertNotNull(target.getComponent(PendingDamageComponent.class));
        target.remove(PendingDamageComponent.class);
        engine.update(0.1f);
        Assertions.assertNull(target.getComponent(PendingDamageComponent.class));
        events.endTriggered = true;
        engine.update(0.1f);
        Assertions.assertFalse(((WeaponStateComponent)weapon.getComponent(WeaponStateComponent.class)).isAttacking);
        Assertions.assertNull(owner.getComponent(AttackingComponent.class));
    }

    private Entity createOwner(float x, float y) {
        Entity owner = new Entity();
        owner.add((Component)new PositionComponent(x, y));
        owner.add((Component)new DamageComponent(12));
        return owner;
    }

    private Entity createTarget(float x, float y) {
        Entity target = new Entity();
        target.add((Component)new PositionComponent(x, y));
        return target;
    }

    private Entity createMeleeWeapon(Entity owner, Entity target, float attackRange, int damageFrame) {
        WeaponComponent wc = new WeaponComponent();
        wc.id = "melee";
        wc.attackRange = attackRange;
        wc.damage = 12.0f;
        wc.damageFrame = damageFrame;
        WeaponStateComponent weaponState = new WeaponStateComponent();
        weaponState.targetEntity = target;
        Entity weaponEntity = new Entity();
        weaponEntity.add((Component)new MeleeWeaponComponent());
        weaponEntity.add((Component)wc);
        weaponEntity.add((Component)weaponState);
        weaponEntity.add((Component)new ParentComponent(owner));
        return weaponEntity;
    }
}