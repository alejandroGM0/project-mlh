package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.factories.CompanionFactory;
import io.github.proyectoM.factories.EnemyFactory;
import io.github.proyectoM.registry.AbstractJsonRegistry;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.CharacterTemplate;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for CompanionFactory and EnemyFactory.
 */
class CharacterFactoriesTest {
    private static final String TEMPLATES_FIELD_NAME = "templates";
    private static final String LOADED_FIELD_NAME = "loaded";

    /**
     * Verifies that clear companion registry.
     */
    @AfterEach
    void clearCompanionRegistry() throws ReflectiveOperationException {
        CharacterFactoriesTest.getCompanionRegistryMap().clear();
    }

    /**
     * Verifies that companion factory constructor rejects null engine.
     */
    @Test
    void companionFactoryConstructorRejectsNullEngine() {
        NullPointerException exception = (NullPointerException)Assertions.assertThrows(NullPointerException.class, () -> new CompanionFactory(null, null, WeaponRegistry.getInstance(), CompanionRegistry.getInstance()));
        Assertions.assertEquals("engine", exception.getMessage());
    }

    /**
     * Verifies that companion factory rejects unknown companion template.
     */
    @Test
    void companionFactoryRejectsUnknownCompanionTemplate() {
        CompanionFactory factory = new CompanionFactory((Engine)new PooledEngine(), null, WeaponRegistry.getInstance(), CompanionRegistry.getInstance());
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.createCompanion("missing_companion", new Vector2()));
    }

    /**
     * Verifies that companion factory rejects null position before physics setup.
     */
    @Test
    void companionFactoryRejectsNullPositionBeforePhysicsSetup() throws ReflectiveOperationException {
        CharacterTemplate template = new CharacterTemplate();
        template.id = "companion_test";
        CharacterFactoriesTest.registerCompanionTemplate(template);
        CompanionFactory factory = new CompanionFactory((Engine)new PooledEngine(), null, WeaponRegistry.getInstance(), CompanionRegistry.getInstance());
        NullPointerException exception = (NullPointerException)Assertions.assertThrows(NullPointerException.class, () -> factory.createCompanion("companion_test", null));
        Assertions.assertEquals("positionMeters", exception.getMessage());
    }

    /**
     * Verifies that enemy factory constructor rejects null engine.
     */
    @Test
    void enemyFactoryConstructorRejectsNullEngine() {
        NullPointerException exception = (NullPointerException)Assertions.assertThrows(NullPointerException.class, () -> new EnemyFactory(null, null, WeaponRegistry.getInstance()));
        Assertions.assertEquals("engine", exception.getMessage());
    }

    /**
     * Verifies that enemy factory rejects null template before physics setup.
     */
    @Test
    void enemyFactoryRejectsNullTemplateBeforePhysicsSetup() {
        EnemyFactory factory = new EnemyFactory((Engine)new PooledEngine(), null, WeaponRegistry.getInstance());
        NullPointerException exception = (NullPointerException)Assertions.assertThrows(NullPointerException.class, () -> factory.createEnemy(null, new Vector2(), 1.0f));
        Assertions.assertEquals("template", exception.getMessage());
    }

    /**
     * Verifies that enemy factory rejects null position before physics setup.
     */
    @Test
    void enemyFactoryRejectsNullPositionBeforePhysicsSetup() {
        EnemyFactory factory = new EnemyFactory((Engine)new PooledEngine(), null, WeaponRegistry.getInstance());
        CharacterTemplate template = new CharacterTemplate();
        template.id = "enemy_test";
        NullPointerException exception = (NullPointerException)Assertions.assertThrows(NullPointerException.class, () -> factory.createEnemy(template, null, 1.0f));
        Assertions.assertEquals("positionMeters", exception.getMessage());
    }

    private static void registerCompanionTemplate(CharacterTemplate template) throws ReflectiveOperationException {
        CharacterFactoriesTest.getCompanionRegistryMap().put(template.id, template);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, CharacterTemplate> getCompanionRegistryMap() throws ReflectiveOperationException {
        CompanionRegistry instance = CompanionRegistry.getInstance();
        Field templatesField = AbstractJsonRegistry.class.getDeclaredField(TEMPLATES_FIELD_NAME);
        templatesField.setAccessible(true);
        Map<String, CharacterTemplate> map = (Map<String, CharacterTemplate>) templatesField.get(instance);
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