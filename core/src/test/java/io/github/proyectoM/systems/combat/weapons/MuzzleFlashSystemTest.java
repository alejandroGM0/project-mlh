package io.github.proyectoM.systems.combat.weapons;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.components.entity.weapon.MuzzleFlashComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.systems.combat.weapons.MuzzleFlashSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for MuzzleFlashSystem.
 */
class MuzzleFlashSystemTest {

    /**
     * Verifies that active flash advances animation and updates light position.
     */
    @Test
    void activeFlashAdvancesAnimationAndUpdatesLightPosition() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new MuzzleFlashSystem());
        Entity owner = new Entity();
        MuzzlePointComponent muzzlePoint = new MuzzlePointComponent();
        muzzlePoint.position.set(32.0f, 64.0f);
        LookAtComponent lookAt = new LookAtComponent();
        lookAt.angle = 1.5707964f;
        owner.add((Component)muzzlePoint);
        owner.add((Component)lookAt);
        engine.addEntity(owner);
        WeaponComponent weapon = new WeaponComponent();
        weapon.id = "weapon";
        WeaponStateComponent weaponState = new WeaponStateComponent();
        weaponState.flashTimer = 0.2f;
        Entity weaponEntity = new Entity();
        weaponEntity.add((Component)weapon);
        weaponEntity.add((Component)weaponState);
        weaponEntity.add((Component)new ParentComponent(owner));
        engine.addEntity(weaponEntity);
        MuzzleFlashComponent flash = new MuzzleFlashComponent();
        flash.weaponEntity = weaponEntity;
        AnimationComponent animation = new AnimationComponent();
        LightComponent light = new LightComponent();
        Entity flashEntity = new Entity();
        flashEntity.add((Component)flash);
        flashEntity.add((Component)animation);
        flashEntity.add((Component)light);
        engine.addEntity(flashEntity);
        engine.update(0.05f);
        Assertions.assertTrue(light.active);
        Assertions.assertEquals(0.1f, animation.stateTime, 1.0E-4f);
        Assertions.assertEquals(1.0f, light.positionMeters.x, 1.0E-4f);
        Assertions.assertEquals(2.0f, light.positionMeters.y, 1.0E-4f);
        Assertions.assertEquals(90.0f, light.coneDirectionDegrees, 1.0E-4f);
        Assertions.assertTrue(light.useCustomPosition);
        Assertions.assertFalse(light.attachToPhysicsBody);
    }

    /**
     * Verifies that inactive flash resets animation and disables light.
     */
    @Test
    void inactiveFlashResetsAnimationAndDisablesLight() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new MuzzleFlashSystem());
        WeaponComponent weapon = new WeaponComponent();
        weapon.id = "weapon";
        WeaponStateComponent weaponState = new WeaponStateComponent();
        Entity weaponEntity = new Entity();
        weaponEntity.add((Component)weapon);
        weaponEntity.add((Component)weaponState);
        engine.addEntity(weaponEntity);
        MuzzleFlashComponent flash = new MuzzleFlashComponent();
        flash.weaponEntity = weaponEntity;
        AnimationComponent animation = new AnimationComponent();
        animation.stateTime = 0.4f;
        LightComponent light = new LightComponent();
        Entity flashEntity = new Entity();
        flashEntity.add((Component)flash);
        flashEntity.add((Component)animation);
        flashEntity.add((Component)light);
        engine.addEntity(flashEntity);
        engine.update(0.05f);
        Assertions.assertEquals(0.0f, animation.stateTime, 1.0E-4f);
        Assertions.assertFalse(light.active);
    }
}