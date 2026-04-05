package io.github.proyectoM.factories;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.companion.SquadMovementComponent;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.templates.CharacterTemplate;
import java.util.Objects;

/** Creates and configures companion entities. */
public class CompanionFactory extends AbstractCharacterFactory {
  private final CompanionRegistry companionRegistry;

  public CompanionFactory(
      Engine engine, World world, WeaponRegistry weaponRegistry, CompanionRegistry companionRegistry) {
    super(engine, world, weaponRegistry);
    this.companionRegistry = Objects.requireNonNull(companionRegistry, "companionRegistry");
  }

  public Entity createCompanion(String companionId, Vector2 positionMeters) {
    CharacterTemplate template = companionRegistry.getRequired(companionId);

    Entity companion = engine.createEntity();
    addComponents(companion, template, Objects.requireNonNull(positionMeters, "positionMeters"));
    engine.addEntity(companion);
    return companion;
  }

  private void addComponents(Entity companion, CharacterTemplate template, Vector2 positionMeters) {
    addBaseStats(companion, positionMeters, (int) template.health, (int) template.damage);
    addPhysicsComponent(companion, positionMeters, CHARACTER_BODY_RADIUS_METERS);
    addCompanionAiComponents(companion, template);
    addMovementComponents(companion);
    addWeaponSystem(companion, engine, template.weaponId);
    addVisualAndAnimationComponents(companion, template.atlas_path, template.weaponId);
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
}
