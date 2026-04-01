/**
 * EnemySpawnerSystem.java
 *
 * <p>A system responsible for continuously spawning zombies based on the state of the WaveSystem.
 * It scales the difficulty (quantity, frequency) according to the current wave. Zombies appear at
 * random positions around the player.
 *
 * <p>Project: ProjectM Author: AlejandroGM0 Date: 2025-07-13
 */
package io.github.proyectoM.systems.enemy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
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

  private static final float DEFAULT_BASE_SPAWN_INTERVAL = 2f;
  private static final float DEFAULT_SPAWN_DISTANCE = 800f;
  private static final int DEFAULT_BASE_ZOMBIES_PER_SPAWN = 1;
  private static final float SPAWN_DISTANCE_VARIATION = 200f;
  private static final int WAVE_FAST_ZOMBIE_THRESHOLD = 3;
  private static final float FAST_ZOMBIE_CHANCE = 0.3f;
  private static final float DIFFICULTY_MULTIPLIER_FACTOR = 0.1f;
  private static final float INITIAL_TIME_SINCE_SPAWN = 0f;
  private static final String ZOMBIE_TYPE_BASIC = "zombie_basic";
  private static final String ZOMBIE_TYPE_FAST = "zombie_fast";

  private final EnemyFactory enemyFactory;
  private final ComponentMapper<PositionComponent> posMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<WaveComponent> waveMapper =
      ComponentMapper.getFor(WaveComponent.class);
  private final ComponentMapper<GameStateComponent> gameStateMapper =
      ComponentMapper.getFor(GameStateComponent.class);

  private final Vector2 spawnPositionBuffer = new Vector2();
  private final Vector2 spawnMetersBuffer = new Vector2();
  private float timeSinceLastSpawn = INITIAL_TIME_SINCE_SPAWN;

  public EnemySpawnerSystem(Engine engine, World world) {
    this.enemyFactory = new EnemyFactory(engine, world);
  }

  @Override
  public void update(float deltaTime) {
    ImmutableArray<Entity> stateEntities =
        getEngine().getEntitiesFor(Family.all(GameStateComponent.class, WaveComponent.class).get());
    if (stateEntities.size() == 0) {
      return;
    }

    Entity globalStateEntity = stateEntities.first();
    GameStateComponent gameState = gameStateMapper.get(globalStateEntity);
    if (gameState.currentState != GameStateComponent.State.RUNNING) {
      return;
    }

    ImmutableArray<Entity> leaderEntities =
        getEngine()
            .getEntitiesFor(
                Family.all(GroupControllerComponent.class, PositionComponent.class).get());
    if (leaderEntities.size() == 0) {
      return;
    }

    Entity virtualLeader = leaderEntities.first();
    WaveComponent wave = waveMapper.get(globalStateEntity);

    timeSinceLastSpawn += deltaTime;
    float currentInterval = calculateSpawnInterval(wave);
    if (timeSinceLastSpawn >= currentInterval) {
      spawnZombies(wave, virtualLeader);
      timeSinceLastSpawn = 0f;
    }
  }

  private float calculateSpawnInterval(WaveComponent wave) {
    return DEFAULT_BASE_SPAWN_INTERVAL / calculateDifficultyMultiplier(wave);
  }

  private int calculateZombiesPerSpawn(WaveComponent wave) {
    return DEFAULT_BASE_ZOMBIES_PER_SPAWN + (wave.currentWave - 1) / 2;
  }

  private void spawnZombies(WaveComponent wave, Entity virtualLeader) {
    PositionComponent virtualLeaderPosition = posMapper.get(virtualLeader);
    int zombiesToSpawn = calculateZombiesPerSpawn(wave);
    for (int i = 0; i < zombiesToSpawn; i++) {
      spawnSingleZombie(virtualLeaderPosition.x, virtualLeaderPosition.y, wave);
    }
  }

  private void spawnSingleZombie(float leaderX, float leaderY, WaveComponent wave) {
    calculateSpawnPosition(leaderX, leaderY);
    spawnMetersBuffer.set(
        spawnPositionBuffer.x * PhysicsConstants.METERS_PER_PIXEL,
        spawnPositionBuffer.y * PhysicsConstants.METERS_PER_PIXEL);

    String zombieType = selectZombieType(wave);
    CharacterTemplate template = EnemyRegistry.getInstance().getTemplate(zombieType);
    enemyFactory.createEnemy(template, spawnMetersBuffer, calculateDifficultyMultiplier(wave));
  }

  private float calculateDifficultyMultiplier(WaveComponent wave) {
    return (1.0f + wave.currentWave) * DIFFICULTY_MULTIPLIER_FACTOR;
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
