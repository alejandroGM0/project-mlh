package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import io.github.proyectoM.components.entity.animation.ActionStateComponent;
import io.github.proyectoM.components.entity.animation.MovementDirectionStateComponent;
import io.github.proyectoM.components.visual.VisualAssetComponent;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.templates.CharacterTemplate;
import java.util.Objects;

/** Creates and configures companion entities. */
public class CompanionFactory extends AbstractCharacterFactory {
  private static final float COMPANION_RADIUS_METERS = 1.25f;

  public CompanionFactory(Engine engine, World world) {
    super(engine, world);
  }

  public Entity createCompanion(String companionId, Vector2 positionMeters) {
    CharacterTemplate template = getRequiredTemplate(companionId);

    Entity companion = engine.createEntity();
    addComponents(companion, template, Objects.requireNonNull(positionMeters, "positionMeters"));
    engine.addEntity(companion);
    return companion;
  }

  private CharacterTemplate getRequiredTemplate(String companionId) {
    CharacterTemplate template = CompanionRegistry.getInstance().getTemplate(companionId);
    if (template == null) {
      throw new IllegalArgumentException("Unknown companion template: " + companionId);
    }
    return template;
  }

  private void addComponents(Entity companion, CharacterTemplate template, Vector2 positionMeters) {
    addBaseStats(companion, positionMeters, (int) template.health, (int) template.damage);
    addPhysicsComponent(companion, positionMeters, COMPANION_RADIUS_METERS);
    addCompanionAiComponents(companion, template);
    addMovementComponents(companion);
    addWeaponSystem(companion, engine, template.weaponId);
    addAnimationComponents(companion, template);
    addCombatComponents(companion);
  }

  private void addCompanionAiComponents(Entity companion, CharacterTemplate template) {
    addAIComponent(companion, template);
    CompanionComponent comp = engine.createComponent(CompanionComponent.class);
    comp.companionType = template.id;
    comp.damage = (int) template.damage;
    companion.add(comp);
  }

  private void addMovementComponents(Entity companion) {
    companion.add(engine.createComponent(SquadMovementComponent.class));
  }

  private void addAnimationComponents(Entity companion, CharacterTemplate template) {
    companion.add(engine.createComponent(MovementDirectionStateComponent.class));
    VisualAssetComponent visual = engine.createComponent(VisualAssetComponent.class);
    visual.visualAssetId = template.atlas_path;
    companion.add(visual);

    int attackVariant = resolveAttackVariant(template.weaponId);
    ActionStateComponent action = createIdleActionState(attackVariant);
    companion.add(action);
    addInitialIdleAnimation(companion, template.atlas_path);
  }
}
