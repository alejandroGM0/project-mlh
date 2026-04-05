package io.github.proyectoM.systems.animation;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.proyectoM.components.entity.animation.AnimEventComponent;
import io.github.proyectoM.components.entity.animation.AnimationComponent;
import io.github.proyectoM.systems.animation.AnimationSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for AnimationSystem.
 */
class AnimationSystemTest {

    /**
     * Verifies that frame events use the animation frame duration instead of a fixed constant.
     */
    @Test
    void frameEventsUseTheAnimationFrameDurationInsteadOfAFixedConstant() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new AnimationSystem());
        Entity entity = new Entity();
        AnimationComponent animation = new AnimationComponent();
        animation.currentAnimation = new Animation(0.25f, (Object[])new TextureRegion[]{new TextureRegion(), new TextureRegion(), new TextureRegion()});
        animation.stateTime = 0.1f;
        AnimEventComponent events = new AnimEventComponent();
        events.defineEvent(AnimEventComponent.AnimEventType.HIT_FRAME, 1);
        entity.add((Component)animation);
        entity.add((Component)events);
        engine.addEntity(entity);
        engine.update(0.2f);
        Assertions.assertTrue(events.hasEvent(AnimEventComponent.AnimEventType.HIT_FRAME));
    }

    /**
     * Verifies that end event triggers when the animation finishes.
     */
    @Test
    void endEventTriggersWhenTheAnimationFinishes() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new AnimationSystem());
        Entity entity = new Entity();
        AnimationComponent animation = new AnimationComponent();
        animation.currentAnimation = new Animation(0.25f, (Object[])new TextureRegion[]{new TextureRegion(), new TextureRegion()});
        AnimEventComponent events = new AnimEventComponent();
        entity.add((Component)animation);
        entity.add((Component)events);
        engine.addEntity(entity);
        engine.update(0.6f);
        Assertions.assertTrue(events.hasEvent(AnimEventComponent.AnimEventType.END));
    }
}