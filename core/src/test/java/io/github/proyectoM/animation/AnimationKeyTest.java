package io.github.proyectoM.animation;

import io.github.proyectoM.animation.AnimationKey;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for AnimationKey cache and validation.
 */
class AnimationKeyTest {

    /**
     * Verifies that get returns the same cached instance for base variant.
     */
    @Test
    void getReturnsTheSameCachedInstanceForBaseVariant() {
        AnimationKey firstKey = AnimationKey.get(ActionStateComponent.ActionType.IDLE, MovementDirectionStateComponent.MovementType.FORWARD, 3);
        AnimationKey secondKey = AnimationKey.get(ActionStateComponent.ActionType.IDLE, MovementDirectionStateComponent.MovementType.FORWARD, 3);
        Assertions.assertSame(firstKey, secondKey);
        Assertions.assertEquals(0, firstKey.animationVariant);
    }

    /**
     * Verifies that get returns the same cached instance for non zero variant.
     */
    @Test
    void getReturnsTheSameCachedInstanceForNonZeroVariant() {
        AnimationKey firstKey = AnimationKey.get(ActionStateComponent.ActionType.ATTACK, MovementDirectionStateComponent.MovementType.STRAFE_LEFT, 5, 2);
        AnimationKey secondKey = AnimationKey.get(ActionStateComponent.ActionType.ATTACK, MovementDirectionStateComponent.MovementType.STRAFE_LEFT, 5, 2);
        Assertions.assertSame(firstKey, secondKey);
        Assertions.assertEquals(2, firstKey.animationVariant);
    }

    /**
     * Verifies that get rejects invalid direction index.
     */
    @Test
    void getRejectsInvalidDirectionIndex() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> AnimationKey.get(ActionStateComponent.ActionType.MOVE, MovementDirectionStateComponent.MovementType.BACKWARDS, 8));
    }

    /**
     * Verifies that get rejects invalid variant index.
     */
    @Test
    void getRejectsInvalidVariantIndex() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> AnimationKey.get(ActionStateComponent.ActionType.HURT, MovementDirectionStateComponent.MovementType.STRAFE_RIGHT, 0, 10));
    }
}