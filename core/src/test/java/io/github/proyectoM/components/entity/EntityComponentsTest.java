package io.github.proyectoM.components.entity;

import com.badlogic.ashley.core.Entity;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.AIComponent;
import io.github.proyectoM.components.entity.InventoryComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for base entity components.
 */
class EntityComponentsTest {

    /**
     * Verifies that ai component uses safe defaults.
     */
    @Test
    void aiComponentUsesSafeDefaults() {
        AIComponent component = new AIComponent();
        Assertions.assertEquals("", component.id);
        Assertions.assertEquals("", component.name);
        Assertions.assertEquals(0.0f, component.currentCooldown, 1.0E-4f);
        Assertions.assertEquals(0.0f, component.spawnTime, 1.0E-4f);
    }

    /**
     * Verifies that ai component constructor stores configured values.
     */
    @Test
    void aiComponentConstructorStoresConfiguredValues() {
        AIComponent component = new AIComponent("enemy_01", "Runner", 4.0f, 2.0f, 12.0f, 7.0f, 1.5f, 90.0f);
        Assertions.assertEquals("enemy_01", component.id);
        Assertions.assertEquals("Runner", component.name);
        Assertions.assertEquals(4.0f, component.speed, 1.0E-4f);
        Assertions.assertEquals(2.0f, component.armor, 1.0E-4f);
        Assertions.assertEquals(12.0f, component.baseDamage, 1.0E-4f);
        Assertions.assertEquals(7.0f, component.baseRange, 1.0E-4f);
        Assertions.assertEquals(1.5f, component.cooldownAttack, 1.0E-4f);
        Assertions.assertEquals(90.0f, component.mass, 1.0E-4f);
        Assertions.assertEquals(0.0f, component.currentCooldown, 1.0E-4f);
        Assertions.assertTrue((component.spawnTime > 0.0f ? 1 : 0) != 0);
    }

    /**
     * Verifies that enemy component uses expected defaults.
     */
    @Test
    void enemyComponentUsesExpectedDefaults() {
        EnemyComponent component = new EnemyComponent();
        Assertions.assertEquals("zombie_basic", component.enemyType);
        Assertions.assertEquals(10, component.scorePoints);
    }

    /**
     * Verifies that inventory component stops accepting weapons at capacity.
     */
    @Test
    void inventoryComponentStopsAcceptingWeaponsAtCapacity() {
        InventoryComponent inventory = new InventoryComponent();
        for (int i = 0; i < 10; ++i) {
            Assertions.assertTrue(inventory.addWeapon(new Entity()));
        }
        Assertions.assertFalse(inventory.hasCapacity());
        Assertions.assertFalse(inventory.addWeapon(new Entity()));
        Assertions.assertEquals(10, inventory.weapons.size);
    }
}