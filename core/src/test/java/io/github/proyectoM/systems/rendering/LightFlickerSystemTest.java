package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.visual.FlickerLightComponent;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.systems.rendering.LightFlickerSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for LightFlickerSystem.
 */
class LightFlickerSystemTest {

    /**
     * Verifies that process entity stores base distance and applies flicker.
     */
    @Test
    void processEntityStoresBaseDistanceAndAppliesFlicker() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new LightFlickerSystem());
        LightComponent light = new LightComponent();
        light.distance = 20.0f;
        FlickerLightComponent flicker = new FlickerLightComponent();
        flicker.amount = 0.25f;
        flicker.speed = 2.0f;
        Entity entity = new Entity();
        entity.add((Component)light);
        entity.add((Component)flicker);
        engine.addEntity(entity);
        engine.update(0.5f);
        Assertions.assertEquals(20.0f, flicker.baseDistance, 1.0E-4f);
        Assertions.assertEquals((20.0f + 5.0f * Math.sin(1.0)), light.distance, 1.0E-4f);
    }

    /**
     * Verifies that process entity restores base distance when flicker is disabled.
     */
    @Test
    void processEntityRestoresBaseDistanceWhenFlickerIsDisabled() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new LightFlickerSystem());
        LightComponent light = new LightComponent();
        light.distance = 4.0f;
        FlickerLightComponent flicker = new FlickerLightComponent();
        flicker.baseDistance = 10.0f;
        flicker.isFlickering = false;
        Entity entity = new Entity();
        entity.add((Component)light);
        entity.add((Component)flicker);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertEquals(10.0f, light.distance, 1.0E-4f);
    }
}