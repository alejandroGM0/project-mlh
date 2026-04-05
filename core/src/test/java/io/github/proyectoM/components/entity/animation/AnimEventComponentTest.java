package io.github.proyectoM.components.entity.animation;

import io.github.proyectoM.components.entity.animation.AnimEventComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for AnimEventComponent.
 */
class AnimEventComponentTest {

    /**
     * Verifies that has event returns true when the event was triggered.
     */
    @Test
    void hasEventReturnsTrueWhenTheEventWasTriggered() {
        AnimEventComponent component = new AnimEventComponent();
        component.triggeredEvents.add(AnimEventComponent.AnimEventType.HIT_FRAME);
        Assertions.assertTrue(component.hasEvent(AnimEventComponent.AnimEventType.HIT_FRAME));
    }

    /**
     * Verifies that has event uses the end flag for end events.
     */
    @Test
    void hasEventUsesTheEndFlagForEndEvents() {
        AnimEventComponent component = new AnimEventComponent();
        component.endTriggered = true;
        Assertions.assertTrue(component.hasEvent(AnimEventComponent.AnimEventType.END));
    }

    /**
     * Verifies that clear triggered resets triggered events and end flag.
     */
    @Test
    void clearTriggeredResetsTriggeredEventsAndEndFlag() {
        AnimEventComponent component = new AnimEventComponent();
        component.triggeredEvents.add(AnimEventComponent.AnimEventType.SOUND_CUE);
        component.endTriggered = true;
        component.clearTriggered();
        Assertions.assertFalse(component.hasEvent(AnimEventComponent.AnimEventType.SOUND_CUE));
        Assertions.assertFalse(component.hasEvent(AnimEventComponent.AnimEventType.END));
    }
}