package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.movement.SteeringComponent;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.CharacterTemplate;
import java.util.Objects;

/** Creates and configures enemy entities. */
public class EnemyFactory extends AbstractCharacterFactory {
  private static final float DEFAULT_DIFFICULTY_MULTIPLIER = 1f;

  public EnemyFactory(Engine engine, World world, WeaponRegistry weaponRegistry) {
    super(engine, world, weaponRegistry);
  }

  public Entity createEnemy(CharacterTemplate template, Vector2 positionMeters) {
    return createEnemy(template, positionMeters, DEFAULT_DIFFICULTY_MULTIPLIER);
  }

  public Entity createEnemy(
      CharacterTemplate template, Vector2 positionMeters, float difficultyMultiplier) {
    CharacterTemplate requiredTemplate = Objects.requireNonNull(template, "template");
    Vector2 requiredPosition = Objects.requireNonNull(positionMeters, "positionMeters");
    Entity enemy = engine.createEntity();

    addBaseComponents(enemy, requiredTemplate, requiredPosition, difficultyMultiplier);
    addBehaviorComponents(enemy, requiredTemplate);
    addPhysicsComponent(enemy, requiredPosition, CHARACTER_BODY_RADIUS_METERS);

    engine.addEntity(enemy);
    return enemy;
  }

  private void addBaseComponents(
      Entity enemy,
      CharacterTemplate template,
      Vector2 positionMeters,
      float difficultyMultiplier) {
    int scaledHealth = Math.round(template.health * difficultyMultiplier);
    int scaledDamage = Math.round(template.damage * difficultyMultiplier);

    addBaseStats(enemy, positionMeters, scaledHealth, scaledDamage);
    addVisualAndAnimationComponents(enemy, template.atlas_path, template.weaponId);
    setDieVariant(enemy, template.dieVariant);
    addWeaponSystem(enemy, engine, template.weaponId);
  }

  /**
   * Sets the die animation variant on the entity's ActionStateComponent.
   *
   * @param entity the entity to configure
   * @param dieVariant the die animation variant index
   */
  private void setDieVariant(Entity entity, int dieVariant) {
    ActionStateComponent action = entity.getComponent(ActionStateComponent.class);
    action.setVariant(ActionStateComponent.ActionType.DIE, dieVariant);
  }

  private void addBehaviorComponents(Entity enemy, CharacterTemplate template) {
    addAIComponent(enemy, template);
    addEnemyComponent(enemy, template);
    enemy.add(engine.createComponent(SteeringComponent.class));
    addCombatComponents(enemy);
  }

  private void addEnemyComponent(Entity enemy, CharacterTemplate template) {
    EnemyComponent enemyComponent = engine.createComponent(EnemyComponent.class);
    enemyComponent.enemyType = template.id;
    enemyComponent.scorePoints = template.scorePoints;
    enemy.add(enemyComponent);
  }
}
