package io.github.proyectoM.components.companion;

import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for companion-related components.
 */
class CompanionComponentsTest {

    /**
     * Verifies that companion component uses expected defaults.
     */
    @Test
    void companionComponentUsesExpectedDefaults() {
        CompanionComponent component = new CompanionComponent();
        Assertions.assertEquals("soldier", component.companionType);
        Assertions.assertEquals(10, component.damage);
        Assertions.assertEquals(1.0f, component.rangeMultiplier, 1.0E-4f);
    }

    /**
     * Verifies that companion component constructor overrides type and damage.
     */
    @Test
    void companionComponentConstructorOverridesTypeAndDamage() {
        CompanionComponent component = new CompanionComponent("medic", 25);
        Assertions.assertEquals("medic", component.companionType);
        Assertions.assertEquals(25, component.damage);
        Assertions.assertEquals(1.0f, component.rangeMultiplier, 1.0E-4f);
    }

    /**
     * Verifies that group controller component uses expected defaults.
     */
    @Test
    void groupControllerComponentUsesExpectedDefaults() {
        GroupControllerComponent component = new GroupControllerComponent();
        Assertions.assertEquals(400.0f, component.movementSpeed, 1.0E-4f);
        Assertions.assertEquals(GroupControllerComponent.DEFAULT_FORMATION, component.currentFormation);
        Assertions.assertEquals(240.0f, component.formationSpacing, 1.0E-4f);
        Assertions.assertFalse(component.formationChanged);
    }

    /**
     * Verifies that squad movement component starts with default index and offset.
     */
    @Test
    void squadMovementComponentStartsWithDefaultIndexAndOffset() {
        SquadMovementComponent component = new SquadMovementComponent();
        Assertions.assertEquals(0, component.memberIndex);
        Assertions.assertNotNull(component.formationOffset);
        Assertions.assertEquals(0.0f, component.formationOffset.x, 1.0E-4f);
        Assertions.assertEquals(0.0f, component.formationOffset.y, 1.0E-4f);
    }
}