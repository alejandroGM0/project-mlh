package io.github.proyectoM.components.entity.visual;

import com.badlogic.gdx.graphics.Color;
import io.github.proyectoM.components.entity.visual.LightComponent;
import io.github.proyectoM.components.entity.visual.OpacityComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for LightAndOpacityComponent.
 */
class LightAndOpacityComponentTest {

    /**
     * Verifies that light component starts with expected defaults.
     */
    @Test
    void lightComponentStartsWithExpectedDefaults() {
        LightComponent component = new LightComponent();
        Assertions.assertEquals(LightComponent.LightType.POINT, component.type);
        Assertions.assertEquals(48.0f, component.distance, 1.0E-4f);
        Assertions.assertEquals(30.0f, component.coneDegree, 1.0E-4f);
        Assertions.assertEquals(0.0f, component.coneDirectionDegrees, 1.0E-4f);
        Assertions.assertEquals(0.0f, component.directionOffsetDegrees, 1.0E-4f);
        Assertions.assertEquals(1.5f, component.softnessLength, 1.0E-4f);
        Assertions.assertEquals(128, component.rays);
        Assertions.assertTrue(component.alignToBodyAngle);
        Assertions.assertTrue(component.active);
        Assertions.assertFalse(component.xray);
        Assertions.assertTrue(component.attachToPhysicsBody);
        Assertions.assertFalse(component.useCustomPosition);
        Assertions.assertEquals(Color.WHITE.r, component.color.r, 1.0E-4f);
        Assertions.assertEquals(Color.WHITE.g, component.color.g, 1.0E-4f);
        Assertions.assertEquals(Color.WHITE.b, component.color.b, 1.0E-4f);
        Assertions.assertEquals(Color.WHITE.a, component.color.a, 1.0E-4f);
        Assertions.assertNotNull(component.positionMeters);
        Assertions.assertNotNull(component.offsetMeters);
    }

    /**
     * Verifies that opacity component starts fully opaque.
     */
    @Test
    void opacityComponentStartsFullyOpaque() {
        OpacityComponent component = new OpacityComponent();
        Assertions.assertEquals(1.0f, component.alpha, 1.0E-4f);
    }
}