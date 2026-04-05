package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.combat.TargetComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.systems.combat.LookAtSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for LookAtSystem.
 */
class LookAtSystemTest {

    /**
     * Verifies that process entity points towards the target using isometric offset.
     */
    @Test
    void processEntityPointsTowardsTheTargetUsingIsometricOffset() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new LookAtSystem());
        Entity target = new Entity();
        target.add((Component)new PositionComponent(10.0f, 0.0f));
        engine.addEntity(target);
        TargetComponent targetComponent = new TargetComponent();
        targetComponent.targetEntity = target;
        LookAtComponent lookAt = new LookAtComponent();
        Entity entity = new Entity();
        entity.add((Component)new PositionComponent(0.0f, 0.0f));
        entity.add((Component)lookAt);
        entity.add((Component)targetComponent);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertEquals(-0.7853982f, lookAt.angle, 1.0E-4f);
    }
}