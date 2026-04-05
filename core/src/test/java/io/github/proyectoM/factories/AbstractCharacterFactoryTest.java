package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.InventoryComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.combat.TargetComponent;
import io.github.proyectoM.components.entity.movement.LookAtComponent;
import io.github.proyectoM.components.entity.weapon.MuzzlePointComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.factories.AbstractCharacterFactory;
import io.github.proyectoM.registry.AbstractJsonRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.WeaponTemplate;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for AbstractCharacterFactory protected methods.
 */
class AbstractCharacterFactoryTest {
    private static final String TEMPLATES_FIELD_NAME = "templates";
    private static final String LOADED_FIELD_NAME = "loaded";

    /**
     * Verifies that clear weapon registry.
     */
    @AfterEach
    void clearWeaponRegistry() throws ReflectiveOperationException {
        AbstractCharacterFactoryTest.getWeaponRegistryMap().clear();
    }

    /**
     * Verifies that add weapon system adds inventory weapon and muzzle point for ranged weapons.
     */
    @Test
    void addWeaponSystemAddsInventoryWeaponAndMuzzlePointForRangedWeapons() throws ReflectiveOperationException {
        AbstractCharacterFactoryTest.registerTemplate(AbstractCharacterFactoryTest.createWeaponTemplate("ranged_test", "ranged", 4));
        PooledEngine engine = new PooledEngine();
        TestCharacterFactory factory = new TestCharacterFactory(engine);
        Entity owner = new Entity();
        factory.addWeaponSystemForTest(owner, engine, "ranged_test");
        InventoryComponent inventory = (InventoryComponent)owner.getComponent(InventoryComponent.class);
        Assertions.assertNotNull(inventory);
        Assertions.assertEquals(1, inventory.weapons.size);
        Assertions.assertNotNull(owner.getComponent(MuzzlePointComponent.class));
        Assertions.assertNotNull(((Entity)inventory.weapons.first()).getComponent(WeaponComponent.class));
    }

    /**
     * Verifies that add weapon system does not add muzzle point for melee weapons.
     */
    @Test
    void addWeaponSystemDoesNotAddMuzzlePointForMeleeWeapons() throws ReflectiveOperationException {
        AbstractCharacterFactoryTest.registerTemplate(AbstractCharacterFactoryTest.createWeaponTemplate("melee_test", "melee", 2));
        PooledEngine engine = new PooledEngine();
        TestCharacterFactory factory = new TestCharacterFactory(engine);
        Entity owner = new Entity();
        factory.addWeaponSystemForTest(owner, engine, "melee_test");
        InventoryComponent inventory = (InventoryComponent)owner.getComponent(InventoryComponent.class);
        Assertions.assertNotNull(inventory);
        Assertions.assertEquals(1, inventory.weapons.size);
        Assertions.assertNull(owner.getComponent(MuzzlePointComponent.class));
    }

    /**
     * Verifies that add weapon system rejects unknown weapon templates.
     */
    @Test
    void addWeaponSystemRejectsUnknownWeaponTemplates() {
        PooledEngine engine = new PooledEngine();
        TestCharacterFactory factory = new TestCharacterFactory(engine);
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.addWeaponSystemForTest(new Entity(), engine, "missing_weapon"));
    }

    /**
     * Verifies that resolve attack variant returns template attack variant.
     */
    @Test
    void resolveAttackVariantReturnsTemplateAttackVariant() throws ReflectiveOperationException {
        AbstractCharacterFactoryTest.registerTemplate(AbstractCharacterFactoryTest.createWeaponTemplate("variant_test", "ranged", 7));
        TestCharacterFactory factory = new TestCharacterFactory(new PooledEngine());
        Assertions.assertEquals(7, factory.resolveAttackVariantForTest("variant_test"));
    }

    /**
     * Verifies that create idle action state configures attack variant and idle action.
     */
    @Test
    void createIdleActionStateConfiguresAttackVariantAndIdleAction() {
        TestCharacterFactory factory = new TestCharacterFactory(new PooledEngine());
        ActionStateComponent actionState = factory.createIdleActionStateForTest(5);
        Assertions.assertEquals(ActionStateComponent.ActionType.IDLE, actionState.actionType);
        Assertions.assertEquals(5, actionState.getVariant(ActionStateComponent.ActionType.ATTACK));
    }

    /**
     * Verifies that add combat components adds look at and target components.
     */
    @Test
    void addCombatComponentsAddsLookAtAndTargetComponents() {
        TestCharacterFactory factory = new TestCharacterFactory(new PooledEngine());
        Entity entity = new Entity();
        factory.addCombatComponentsForTest(entity);
        Assertions.assertNotNull(entity.getComponent(LookAtComponent.class));
        Assertions.assertNotNull(entity.getComponent(TargetComponent.class));
    }

    private static WeaponTemplate createWeaponTemplate(String id, String type, int attackVariant) {
        WeaponTemplate template = new WeaponTemplate();
        template.id = id;
        template.type = type;
        template.bulletType = "bullet_basic";
        template.atlas = "";
        template.flashAtlas = "";
        template.attackVariant = attackVariant;
        return template;
    }

    private static void registerTemplate(WeaponTemplate template) throws ReflectiveOperationException {
        AbstractCharacterFactoryTest.getWeaponRegistryMap().put(template.id.toLowerCase(Locale.ROOT), template);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, WeaponTemplate> getWeaponRegistryMap() throws ReflectiveOperationException {
        WeaponRegistry instance = WeaponRegistry.getInstance();
        Field templatesField = AbstractJsonRegistry.class.getDeclaredField(TEMPLATES_FIELD_NAME);
        templatesField.setAccessible(true);
        Map<String, WeaponTemplate> map = (Map<String, WeaponTemplate>) templatesField.get(instance);
        if (map == null) {
            map = new LinkedHashMap<>();
            templatesField.set(instance, map);
            Field loadedField = AbstractJsonRegistry.class.getDeclaredField(LOADED_FIELD_NAME);
            loadedField.setAccessible(true);
            loadedField.set(instance, true);
        }
        return map;
    }

    private static final class TestCharacterFactory
    extends AbstractCharacterFactory {
        private TestCharacterFactory(Engine engine) {
            super(engine, null, WeaponRegistry.getInstance());
        }

        private void addWeaponSystemForTest(Entity entity, PooledEngine engine, String weaponId) {
            this.addWeaponSystem(entity, (Engine)engine, weaponId);
        }

        private int resolveAttackVariantForTest(String weaponId) {
            return this.resolveAttackVariant(weaponId);
        }

        private ActionStateComponent createIdleActionStateForTest(int attackVariant) {
            return this.createIdleActionState(attackVariant);
        }

        private void addCombatComponentsForTest(Entity entity) {
            this.addCombatComponents(entity);
        }
    }
}