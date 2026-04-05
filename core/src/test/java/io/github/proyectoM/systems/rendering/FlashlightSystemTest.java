package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.systems.rendering.FlashlightSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for FlashlightSystem.
 */
class FlashlightSystemTest {

    /**
     * Verifies that process entity updates cone light from muzzle position and facing angle.
     */
    @Test
    void processEntityUpdatesConeLightFromMuzzlePositionAndFacingAngle() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new FlashlightSystem());
        MuzzlePointComponent muzzle = new MuzzlePointComponent();
        muzzle.position.set(40.0f, 20.0f);
        LookAtComponent lookAt = new LookAtComponent();
        lookAt.angle = 1.5707964f;
        LightComponent light = new LightComponent();
        light.type = LightComponent.LightType.CONE;
        light.attachToPhysicsBody = true;
        light.useCustomPosition = false;
        Entity entity = new Entity();
        entity.add((Component)muzzle);
        entity.add((Component)lookAt);
        entity.add((Component)light);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertEquals(1.25f, light.positionMeters.x, 1.0E-4f);
        Assertions.assertEquals(0.625f, light.positionMeters.y, 1.0E-4f);
        Assertions.assertEquals(90.0f, light.coneDirectionDegrees, 1.0E-4f);
        Assertions.assertTrue(light.useCustomPosition);
        Assertions.assertFalse(light.attachToPhysicsBody);
    }

    /**
     * Verifies that process entity ignores inactive or non cone lights.
     */
    @Test
    void processEntityIgnoresInactiveOrNonConeLights() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new FlashlightSystem());
        MuzzlePointComponent muzzle = new MuzzlePointComponent();
        muzzle.position.set(10.0f, 15.0f);
        LookAtComponent lookAt = new LookAtComponent();
        lookAt.angle = 1.0f;
        LightComponent light = new LightComponent();
        light.active = false;
        light.type = LightComponent.LightType.CONE;
        light.positionMeters.set(1.0f, 2.0f);
        Entity entity = new Entity();
        entity.add((Component)muzzle);
        entity.add((Component)lookAt);
        entity.add((Component)light);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertEquals(1.0f, light.positionMeters.x, 1.0E-4f);
        Assertions.assertEquals(2.0f, light.positionMeters.y, 1.0E-4f);
    }
}