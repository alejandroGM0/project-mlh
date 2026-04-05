package io.github.proyectoM.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.combat.DeadComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.visual.OpacityComponent;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.ScoreComponent;

/** Marks entities as dead and updates global combat state when a death occurs. */
public class DamageCleanupSystem extends IteratingSystem {
  private final ComponentMapper<HealthComponent> healthMapper =
      ComponentMapper.getFor(HealthComponent.class);
  private final ComponentMapper<GameStateComponent> gameStateMapper =
      ComponentMapper.getFor(GameStateComponent.class);
  private final ComponentMapper<ScoreComponent> scoreMapper =
      ComponentMapper.getFor(ScoreComponent.class);

  private Entity globalStateEntity;

  private ImmutableArray<Entity> globalStateEntities;
  private ImmutableArray<Entity> aliveCompanions;

  public DamageCleanupSystem() {
    super(Family.all(HealthComponent.class).exclude(DeadComponent.class).get());
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    globalStateEntities =
        engine.getEntitiesFor(Family.all(GameStateComponent.class, ScoreComponent.class).get());
    aliveCompanions =
        engine.getEntitiesFor(
            Family.all(CompanionComponent.class, HealthComponent.class)
                .exclude(DeadComponent.class)
                .get());
  }

  @Override
  public void update(float deltaTime) {
    globalStateEntity = findGlobalStateEntity();
    super.update(deltaTime);
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    HealthComponent health = healthMapper.get(entity);
    if (health.currentHealth > 0) {
      return;
    }

    markEntityAsDead(entity);
  }

  private Entity findGlobalStateEntity() {
    if (globalStateEntities.size() == 0) {
      return null;
    }

    return globalStateEntities.first();
  }

  private void markEntityAsDead(Entity entity) {
    entity.add(getEngine().createComponent(DeadComponent.class));
    entity.add(getEngine().createComponent(OpacityComponent.class));
    handleEntityDeath(entity);
    destroyPhysicsBody(entity);
  }

  private void handleEntityDeath(Entity entity) {
    if (entity.getComponent(CompanionComponent.class) != null) {
      handleCompanionDeath();
      return;
    }
    if (entity.getComponent(EnemyComponent.class) != null) {
      incrementEnemyKillCount();
    }
  }

  private void handleCompanionDeath() {
    if (globalStateEntity == null) {
      return;
    }

    if (aliveCompanions.size() <= 1) {
      GameStateComponent gameState = gameStateMapper.get(globalStateEntity);
      gameState.currentState = GameStateComponent.State.GAME_OVER;
    }
  }

  private void incrementEnemyKillCount() {
    if (globalStateEntity == null) {
      return;
    }

    ScoreComponent score = scoreMapper.get(globalStateEntity);
    score.enemiesKilled++;
  }

  private void destroyPhysicsBody(Entity entity) {
    PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
    if (physicsComponent == null || physicsComponent.body == null) {
      return;
    }

    physicsComponent.body.getWorld().destroyBody(physicsComponent.body);
    physicsComponent.body = null;
  }
}
