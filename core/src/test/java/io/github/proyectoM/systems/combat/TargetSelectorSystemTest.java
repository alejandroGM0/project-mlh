package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.systems.combat.TargetSelectorSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for TargetSelectorSystem.
 */
class TargetSelectorSystemTest {

    /**
     * Verifies that companion owned weapon targets the nearest living enemy within range.
     */
    @Test
    void companionOwnedWeaponTargetsTheNearestLivingEnemyWithinRange() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new TargetSelectorSystem());
        Entity companion = new Entity();
        CompanionComponent companionComponent = new CompanionComponent();
        companionComponent.rangeMultiplier = 2.0f;
        companion.add((Component)companionComponent);
        engine.addEntity(companion);
        Entity weapon = this.createWeaponEntity(companion, 50.0f, 0.0f, 0.0f);
        engine.addEntity(weapon);
        Entity nearEnemy = this.createEnemyEntity(40.0f, 0.0f, false);
        Entity farEnemy = this.createEnemyEntity(120.0f, 0.0f, false);
        engine.addEntity(nearEnemy);
        engine.addEntity(farEnemy);
        engine.update(0.2f);
        Assertions.assertSame(nearEnemy, ((WeaponStateComponent)weapon.getComponent(WeaponStateComponent.class)).targetEntity);
    }

    /**
     * Verifies that non companion weapon targets the nearest living companion.
     */
    @Test
    void nonCompanionWeaponTargetsTheNearestLivingCompanion() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new TargetSelectorSystem());
        Entity enemyOwner = new Entity();
        enemyOwner.add((Component)new EnemyComponent());
        engine.addEntity(enemyOwner);
        Entity weapon = this.createWeaponEntity(enemyOwner, 10.0f, 0.0f, 0.0f);
        engine.addEntity(weapon);
        Entity aliveCompanion = new Entity();
        aliveCompanion.add((Component)new CompanionComponent());
        aliveCompanion.add((Component)new PositionComponent(25.0f, 0.0f));
        engine.addEntity(aliveCompanion);
        Entity deadCompanion = new Entity();
        deadCompanion.add((Component)new CompanionComponent());
        deadCompanion.add((Component)new PositionComponent(10.0f, 0.0f));
        deadCompanion.add((Component)new DeadComponent());
        engine.addEntity(deadCompanion);
        engine.update(0.2f);
        Assertions.assertSame(aliveCompanion, ((WeaponStateComponent)weapon.getComponent(WeaponStateComponent.class)).targetEntity);
    }

    /**
     * Verifies that companion owned weapon leaves target null when nothing is in range.
     */
    @Test
    void companionOwnedWeaponLeavesTargetNullWhenNothingIsInRange() {
        PooledEngine engine = new PooledEngine();
        engine.addSystem((EntitySystem)new TargetSelectorSystem());
        Entity companion = new Entity();
        companion.add((Component)new CompanionComponent());
        engine.addEntity(companion);
        Entity weapon = this.createWeaponEntity(companion, 20.0f, 0.0f, 0.0f);
        engine.addEntity(weapon);
        engine.addEntity(this.createEnemyEntity(100.0f, 0.0f, false));
        engine.update(0.2f);
        Assertions.assertNull(((WeaponStateComponent)weapon.getComponent(WeaponStateComponent.class)).targetEntity);
    }

    private Entity createWeaponEntity(Entity parent, float targetRange, float x, float y) {
        WeaponComponent wc = new WeaponComponent();
        wc.id = "test-weapon";
        wc.targetRange = targetRange;
        Entity weapon = new Entity();
        weapon.add((Component)wc);
        weapon.add((Component)new WeaponStateComponent());
        weapon.add((Component)new ParentComponent(parent));
        weapon.add((Component)new PositionComponent(x, y));
        return weapon;
    }

    private Entity createEnemyEntity(float x, float y, boolean dead) {
        Entity enemy = new Entity();
        enemy.add((Component)new EnemyComponent());
        enemy.add((Component)new PositionComponent(x, y));
        if (dead) {
            enemy.add((Component)new DeadComponent());
        }
        return enemy;
    }
}