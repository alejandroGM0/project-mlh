package io.github.proyectoM.components.sound;

import io.github.proyectoM.components.sound.SoundCategory;
import io.github.proyectoM.components.sound.SoundEmitterComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for SoundEmitterComponent.
 */
class SoundEmitterComponentTest {

    /**
     * Verifies that default constructor uses expected defaults.
     */
    @Test
    void defaultConstructorUsesExpectedDefaults() {
        SoundEmitterComponent component = new SoundEmitterComponent();
        Assertions.assertEquals(SoundCategory.SFX, component.soundCategory);
        Assertions.assertEquals("", component.soundName);
        Assertions.assertEquals(1.0f, component.volume, 1.0E-4f);
        Assertions.assertTrue(component.is3D);
        Assertions.assertEquals(500.0f, component.maxDistance, 1.0E-4f);
        Assertions.assertFalse(component.shouldPlay);
        Assertions.assertEquals(0.0f, component.timer, 1.0E-4f);
    }

    /**
     * Verifies that configure updates core sound state and returns same instance.
     */
    @Test
    void configureUpdatesCoreSoundStateAndReturnsSameInstance() {
        SoundEmitterComponent component = new SoundEmitterComponent();
        SoundEmitterComponent returned = component.configure("ui_click", SoundCategory.UI, 0.4f);
        Assertions.assertSame(component, returned);
        Assertions.assertEquals("ui_click", component.soundName);
        Assertions.assertEquals(SoundCategory.UI, component.soundCategory);
        Assertions.assertEquals(0.4f, component.volume, 1.0E-4f);
        Assertions.assertTrue(component.shouldPlay);
    }

    /**
     * Verifies that configure spatial updates spatial state.
     */
    @Test
    void configureSpatialUpdatesSpatialState() {
        SoundEmitterComponent component = new SoundEmitterComponent();
        component.configureSpatial("wind", SoundCategory.AMBIENT, 0.7f, false, 250.0f);
        Assertions.assertEquals("wind", component.soundName);
        Assertions.assertEquals(SoundCategory.AMBIENT, component.soundCategory);
        Assertions.assertEquals(0.7f, component.volume, 1.0E-4f);
        Assertions.assertFalse(component.is3D);
        Assertions.assertEquals(250.0f, component.maxDistance, 1.0E-4f);
        Assertions.assertTrue(component.shouldPlay);
    }

    /**
     * Verifies that play and stop toggle playback flag.
     */
    @Test
    void playAndStopTogglePlaybackFlag() {
        SoundEmitterComponent component = new SoundEmitterComponent();
        component.play();
        Assertions.assertTrue(component.shouldPlay);
        component.stop();
        Assertions.assertFalse(component.shouldPlay);
    }
}