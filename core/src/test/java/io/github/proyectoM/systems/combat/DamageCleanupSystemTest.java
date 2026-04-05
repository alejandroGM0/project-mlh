package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.visual.OpacityComponent;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.ScoreComponent;
import io.github.proyectoM.systems.combat.DamageCleanupSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for DamageCleanupSystem.
 */
class DamageCleanupSystemTest {

    /**
     * Verifies that enemy death increments enemies killed and marks the entity dead.
     */
    @Test
    void enemyDeathIncrementsEnemiesKilledAndMarksTheEntityDead() {
        PooledEngine engine = new PooledEngine();
        Entity globalStateEntity = this.createGlobalStateEntity();
        engine.addEntity(globalStateEntity);
        engine.addSystem((EntitySystem)new DamageCleanupSystem());
        Entity enemy = new Entity();
        enemy.add((Component)new HealthComponent(100, 0));
        enemy.add((Component)new EnemyComponent());
        engine.addEntity(enemy);
        engine.update(0.1f);
        ScoreComponent score = (ScoreComponent)globalStateEntity.getComponent(ScoreComponent.class);
        Assertions.assertEquals(1, score.enemiesKilled);
        Assertions.assertNotNull(enemy.getComponent(DeadComponent.class));
        Assertions.assertNotNull(enemy.getComponent(OpacityComponent.class));
    }

    /**
     * Verifies that last living companion death sets game over.
     */
    @Test
    void lastLivingCompanionDeathSetsGameOver() {
        PooledEngine engine = new PooledEngine();
        Entity globalStateEntity = this.createGlobalStateEntity();
        engine.addEntity(globalStateEntity);
        engine.addSystem((EntitySystem)new DamageCleanupSystem());
        Entity companion = new Entity();
        companion.add((Component)new HealthComponent(100, 0));
        companion.add((Component)new CompanionComponent());
        engine.addEntity(companion);
        engine.update(0.1f);
        GameStateComponent gameState = (GameStateComponent)globalStateEntity.getComponent(GameStateComponent.class);
        Assertions.assertEquals(GameStateComponent.State.GAME_OVER, gameState.currentState);
    }

    private Entity createGlobalStateEntity() {
        Entity entity = new Entity();
        entity.add((Component)new GameStateComponent());
        entity.add((Component)new ScoreComponent());
        return entity;
    }
}