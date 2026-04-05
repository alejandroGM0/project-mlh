package io.github.proyectoM.components.entity.visual;

import io.github.proyectoM.components.entity.visual.FlickerLightComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for FlickerLightComponent.
 */
class FlickerLightComponentTest {

    /**
     * Verifies that reset restores default values.
     */
    @Test
    void resetRestoresDefaultValues() {
        FlickerLightComponent component = new FlickerLightComponent();
        component.baseDistance = 10.0f;
        component.amount = 0.4f;
        component.speed = 9.0f;
        component.timer = 2.0f;
        component.isFlickering = false;
        component.reset();
        Assertions.assertEquals(0.0f, component.baseDistance, 1.0E-4f);
        Assertions.assertEquals(0.15f, component.amount, 1.0E-4f);
        Assertions.assertEquals(5.0f, component.speed, 1.0E-4f);
        Assertions.assertEquals(0.0f, component.timer, 1.0E-4f);
        Assertions.assertTrue(component.isFlickering);
    }
}