package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.visual.OpacityComponent;
import io.github.proyectoM.systems.combat.DeathCleanupSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for DeathCleanupSystem.
 */
class DeathCleanupSystemTest {

    /**
     * Verifies that process entity flags animation finished and starts fade after threshold.
     */
    @Test
    void processEntityFlagsAnimationFinishedAndStartsFadeAfterThreshold() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new DeathCleanupSystem());
        Entity entity = new Entity();
        DeadComponent dead = new DeadComponent();
        dead.timeSinceDeath = 8.5f;
        AnimationComponent animation = new AnimationComponent();
        animation.currentAnimation = new Animation(0.25f, (Object[])new TextureRegion[]{new TextureRegion(), new TextureRegion()});
        animation.stateTime = 0.6f;
        OpacityComponent opacity = new OpacityComponent();
        entity.add((Component)dead);
        entity.add((Component)animation);
        entity.add((Component)opacity);
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertTrue(dead.animationFinished);
        Assertions.assertTrue((opacity.alpha < 1.0f ? 1 : 0) != 0);
    }

    /**
     * Verifies that process entity removes corpse when lifetime expires.
     */
    @Test
    void processEntityRemovesCorpseWhenLifetimeExpires() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new DeathCleanupSystem());
        Entity entity = new Entity();
        DeadComponent dead = new DeadComponent();
        dead.timeSinceDeath = 60.0f;
        entity.add((Component)dead);
        entity.add((Component)new AnimationComponent());
        entity.add((Component)new OpacityComponent());
        engine.addEntity(entity);
        engine.update(0.1f);
        Assertions.assertFalse(engine.getEntities().contains(entity, true));
    }
}