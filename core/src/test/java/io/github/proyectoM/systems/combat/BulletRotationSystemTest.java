package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.weapon.BulletComponent;
import io.github.proyectoM.components.visual.SpriteComponent;
import io.github.proyectoM.systems.combat.BulletRotationSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for BulletRotationSystem.
 */
class BulletRotationSystemTest {

    /**
     * Verifies that process entity rotates using the shortest angular path.
     */
    @Test
    void processEntityRotatesUsingTheShortestAngularPath() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new BulletRotationSystem());
        BulletComponent bullet = new BulletComponent();
        bullet.isRotating = true;
        bullet.targetRotation = 350.0f;
        bullet.rotationSpeed = 90.0f;
        SpriteComponent sprite = new SpriteComponent();
        sprite.angle = 10.0f;
        Entity entity = new Entity();
        entity.add((Component)bullet);
        entity.add((Component)sprite);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertTrue((sprite.angle < 10.0f ? 1 : 0) != 0);
        Assertions.assertTrue(bullet.isRotating);
    }

    /**
     * Verifies that process entity stops rotating when close enough to the target angle.
     */
    @Test
    void processEntityStopsRotatingWhenCloseEnoughToTheTargetAngle() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new BulletRotationSystem());
        BulletComponent bullet = new BulletComponent();
        bullet.isRotating = true;
        bullet.targetRotation = 10.5f;
        SpriteComponent sprite = new SpriteComponent();
        sprite.angle = 10.0f;
        Entity entity = new Entity();
        entity.add((Component)bullet);
        entity.add((Component)sprite);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertEquals(10.5f, sprite.angle);
        Assertions.assertFalse(bullet.isRotating);
    }
}