package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.types.MeleeWeaponComponent;
import io.github.proyectoM.components.entity.weapon.types.RangedWeaponComponent;
import io.github.proyectoM.factories.WeaponFactory;
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
 * Test suite for WeaponFactory weapon creation.
 */
class WeaponFactoryTest {
    private static final String TEMPLATES_FIELD_NAME = "templates";
    private static final String LOADED_FIELD_NAME = "loaded";

    /**
     * Verifies that clear weapon registry.
     */
    @AfterEach
    void clearWeaponRegistry() throws ReflectiveOperationException {
        WeaponFactoryTest.getWeaponRegistryMap().clear();
    }

    /**
     * Verifies that create weapon builds ranged weapon with expected core components.
     */
    @Test
    void createWeaponBuildsRangedWeaponWithExpectedCoreComponents() throws ReflectiveOperationException {
        WeaponFactoryTest.registerTemplate(WeaponFactoryTest.createTemplate("test_ranged", "ranged", "bullet_basic"));
        PooledEngine engine = new PooledEngine();
        Entity owner = new Entity();
        Entity weapon = WeaponFactory.createWeapon((Engine)engine, (String)"test_ranged", (Entity)owner);
        WeaponComponent weaponComponent = (WeaponComponent)weapon.getComponent(WeaponComponent.class);
        RangedWeaponComponent rangedComponent = (RangedWeaponComponent)weapon.getComponent(RangedWeaponComponent.class);
        ActionStateComponent actionState = (ActionStateComponent)weapon.getComponent(ActionStateComponent.class);
        ParentComponent parent = (ParentComponent)weapon.getComponent(ParentComponent.class);
        Assertions.assertNotNull(weaponComponent);
        Assertions.assertNotNull(rangedComponent);
        Assertions.assertNotNull(actionState);
        Assertions.assertNotNull(weapon.getComponent(PositionComponent.class));
        Assertions.assertSame(owner, parent.parent);
        Assertions.assertEquals("test_ranged", weaponComponent.id);
        Assertions.assertEquals("bullet_basic", rangedComponent.bulletType);
        Assertions.assertEquals(ActionStateComponent.ActionType.IDLE, actionState.actionType);
        Assertions.assertEquals(3, actionState.getVariant(ActionStateComponent.ActionType.ATTACK));
        Assertions.assertNull(weapon.getComponent(MeleeWeaponComponent.class));
    }

    /**
     * Verifies that create weapon builds melee weapon specific component.
     */
    @Test
    void createWeaponBuildsMeleeWeaponSpecificComponent() throws ReflectiveOperationException {
        WeaponFactoryTest.registerTemplate(WeaponFactoryTest.createTemplate("test_melee", "melee", ""));
        PooledEngine engine = new PooledEngine();
        Entity owner = new Entity();
        Entity weapon = WeaponFactory.createWeapon((Engine)engine, (String)"test_melee", (Entity)owner);
        Assertions.assertNotNull(weapon.getComponent(MeleeWeaponComponent.class));
        Assertions.assertNull(weapon.getComponent(RangedWeaponComponent.class));
    }

    /**
     * Verifies that create weapon rejects unknown template.
     */
    @Test
    void createWeaponRejectsUnknownTemplate() {
        PooledEngine engine = new PooledEngine();
        Entity owner = new Entity();
        Assertions.assertThrows(IllegalArgumentException.class, () -> WeaponFactory.createWeapon((Engine)engine, (String)"missing_weapon", (Entity)owner));
    }

    private static WeaponTemplate createTemplate(String id, String type, String bulletType) {
        WeaponTemplate template = new WeaponTemplate();
        template.id = id;
        template.type = type;
        template.bulletType = bulletType;
        template.atlas = "";
        template.flashAtlas = "";
        template.attackVariant = 3;
        template.attackRange = 150.0f;
        template.targetRange = 300.0f;
        template.attackSpeed = 0.5f;
        template.damage = 12.0f;
        template.damageFrame = 9;
        return template;
    }

    private static void registerTemplate(WeaponTemplate template) throws ReflectiveOperationException {
        WeaponFactoryTest.getWeaponRegistryMap().put(template.id.toLowerCase(Locale.ROOT), template);
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
}