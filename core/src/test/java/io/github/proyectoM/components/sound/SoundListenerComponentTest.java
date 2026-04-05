package io.github.proyectoM.components.sound;

import io.github.proyectoM.components.sound.SoundListenerComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for SoundListenerComponent.
 */
class SoundListenerComponentTest {

    /**
     * Verifies that default constructor starts active.
     */
    @Test
    void defaultConstructorStartsActive() {
        SoundListenerComponent component = new SoundListenerComponent();
        Assertions.assertTrue(component.active);
    }

    /**
     * Verifies that constructor allows explicit active state.
     */
    @Test
    void constructorAllowsExplicitActiveState() {
        SoundListenerComponent component = new SoundListenerComponent(false);
        Assertions.assertFalse(component.active);
    }
}