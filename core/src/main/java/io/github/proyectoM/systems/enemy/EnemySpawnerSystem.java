package io.github.proyectoM.systems.enemy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.proyectoM.components.companion.GroupControllerComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.WaveComponent;
import io.github.proyectoM.factories.EnemyFactory;
import io.github.proyectoM.physics.PhysicsConstants;
import io.github.proyectoM.registry.EnemyRegistry;
import io.github.proyectoM.templates.CharacterTemplate;

/** A system that continuously spawns enemies based on the current wave. */
public class EnemySpawnerSystem extends EntitySystem {

  private static final float DEFAULT_SPAWN_DISTANCE = 800f;
  private static final float SPAWN_DISTANCE_VARIATION = 200f;
  private static final int WAVE_FAST_ZOMBIE_THRESHOLD = 3;
  private static final float FAST_ZOMBIE_CHANCE = 0.3f;
  private static final float INITIAL_TIME_SINCE_SPAWN = 0f;
  private static final String ZOMBIE_TYPE_BASIC = "zombie_basic";
  private static final String ZOMBIE_TYPE_FAST = "zombie_fast";

  private final EnemyFactory enemyFactory;
  private final EnemyRegistry enemyRegistry;
  private final ComponentMapper<PositionComponent> posMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<WaveComponent> waveMapper =
      ComponentMapper.getFor(WaveComponent.class);
  private final ComponentMapper<GameStateComponent> gameStateMapper =
      ComponentMapper.getFor(GameStateComponent.class);

  private final Vector2 spawnPositionBuffer = new Vector2();
  private final Vector2 spawnMetersBuffer = new Vector2();
  private float timeSinceLastSpawn = INITIAL_TIME_SINCE_SPAWN;

  private ImmutableArray<Entity> stateEntities;
  private ImmutableArray<Entity> leaderEntities;

  /**
   * Creates the spawner with an injected factory and registry.
   *
   * @param enemyFactory  shared factory used to create enemy entities
   * @param enemyRegistry registry holding enemy templates
   */
  public EnemySpawnerSystem(EnemyFactory enemyFactory, EnemyRegistry enemyRegistry) {
    this.enemyFactory = enemyFactory;
    this.enemyRegistry = enemyRegistry;
  }

  @Override
  public void addedToEngine(Engine engine) {
    stateEntities =
        engine.getEntitiesFor(Family.all(GameStateComponent.class, WaveComponent.class).get());
    leaderEntities =
        engine.getEntitiesFor(
            Family.all(GroupControllerComponent.class, PositionComponent.class).get());
  }

  @Override
  public void update(float deltaTime) {
    if (stateEntities.size() == 0) {
      return;
    }

    Entity globalStateEntity = stateEntities.first();
    GameStateComponent gameState = gameStateMapper.get(globalStateEntity);
    if (gameState.currentState != GameStateComponent.State.RUNNING) {
      return;
    }

    if (leaderEntities.size() == 0) {
      return;
    }

    Entity virtualLeader = leaderEntities.first();
    WaveComponent wave = waveMapper.get(globalStateEntity);

    timeSinceLastSpawn += deltaTime;
    if (timeSinceLastSpawn >= wave.spawnInterval) {
      spawnZombies(wave, virtualLeader);
      timeSinceLastSpawn = 0f;
    }
  }

  private void spawnZombies(WaveComponent wave, Entity virtualLeader) {
    PositionComponent virtualLeaderPosition = posMapper.get(virtualLeader);
    for (int i = 0; i < wave.zombiesPerSpawn; i++) {
      spawnSingleZombie(virtualLeaderPosition.x, virtualLeaderPosition.y, wave);
    }
  }

  private void spawnSingleZombie(float leaderX, float leaderY, WaveComponent wave) {
    calculateSpawnPosition(leaderX, leaderY);
    spawnMetersBuffer.set(
        spawnPositionBuffer.x * PhysicsConstants.METERS_PER_PIXEL,
        spawnPositionBuffer.y * PhysicsConstants.METERS_PER_PIXEL);

    String zombieType = selectZombieType(wave);
    CharacterTemplate template = enemyRegistry.getTemplate(zombieType);
    enemyFactory.createEnemy(template, spawnMetersBuffer, wave.difficultyMultiplier);
  }

  private void calculateSpawnPosition(float leaderX, float leaderY) {
    float angle = MathUtils.random() * MathUtils.PI2;
    float distance = DEFAULT_SPAWN_DISTANCE + MathUtils.random() * SPAWN_DISTANCE_VARIATION;
    float spawnX = leaderX + (float) Math.cos(angle) * distance;
    float spawnY = leaderY + (float) Math.sin(angle) * distance;
    spawnPositionBuffer.set(spawnX, spawnY);
  }

  private String selectZombieType(WaveComponent wave) {
    if (wave.currentWave >= WAVE_FAST_ZOMBIE_THRESHOLD && MathUtils.random() < FAST_ZOMBIE_CHANCE) {
      return ZOMBIE_TYPE_FAST;
    } else {
      return ZOMBIE_TYPE_BASIC;
    }
  }
}
