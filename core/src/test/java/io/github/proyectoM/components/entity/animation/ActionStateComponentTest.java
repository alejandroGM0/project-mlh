package io.github.proyectoM.components.entity.animation;

import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for ActionStateComponent states and variants.
 */
class ActionStateComponentTest {

    /**
     * Verifies that get variant returns zero when no variant was defined.
     */
    @Test
    void getVariantReturnsZeroWhenNoVariantWasDefined() {
        ActionStateComponent component = new ActionStateComponent();
        Assertions.assertEquals(0, component.getVariant(ActionStateComponent.ActionType.ATTACK));
    }

    /**
     * Verifies that get current variant uses the current action type.
     */
    @Test
    void getCurrentVariantUsesTheCurrentActionType() {
        ActionStateComponent component = new ActionStateComponent();
        component.actionType = ActionStateComponent.ActionType.DIE;
        component.setVariant(ActionStateComponent.ActionType.DIE, 3);
        Assertions.assertEquals(3, component.getCurrentVariant());
    }
}