package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.combat.PendingDamageComponent;
import io.github.proyectoM.systems.combat.DamageSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for DamageSystem.
 */
class DamageSystemTest {

    /**
     * Verifies that process entity applies pending damage and removes the marker.
     */
    @Test
    void processEntityAppliesPendingDamageAndRemovesTheMarker() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new DamageSystem());
        Entity entity = new Entity();
        entity.add((Component)new HealthComponent(100));
        entity.add((Component)new PendingDamageComponent(12.8f));
        engine.addEntity(entity);
        engine.update(0.1f);
        HealthComponent health = (HealthComponent)entity.getComponent(HealthComponent.class);
        Assertions.assertEquals(87, health.currentHealth);
        Assertions.assertNull(entity.getComponent(PendingDamageComponent.class));
    }
}