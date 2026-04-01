package io.github.proyectoM.systems.combat.weapons;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.proyectoM.components.entity.ParentComponent;
import io.github.proyectoM.components.entity.animation.AnimEventComponent;
import io.github.proyectoM.components.entity.animation.AnimEventComponent.AnimEventType;
import io.github.proyectoM.components.entity.combat.AttackingComponent;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.combat.PendingDamageComponent;
import io.github.proyectoM.components.entity.movement.PhysicsComponent;
import io.github.proyectoM.components.entity.movement.PositionComponent;
import io.github.proyectoM.components.entity.weapon.WeaponComponent;
import io.github.proyectoM.components.entity.weapon.types.MeleeWeaponComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Drives melee weapon attacks using animation events for hit timing and completion. */
public class MeleeWeaponSystem extends IteratingSystem {
  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<ParentComponent> parentMapper =
      ComponentMapper.getFor(ParentComponent.class);
  private final ComponentMapper<PositionComponent> positionMapper =
      ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<AnimEventComponent> eventMapper =
      ComponentMapper.getFor(AnimEventComponent.class);
  private final ComponentMapper<AttackingComponent> attackingMapper =
      ComponentMapper.getFor(AttackingComponent.class);
  private final ComponentMapper<DamageComponent> damageMapper =
      ComponentMapper.getFor(DamageComponent.class);
  private final ComponentMapper<PhysicsComponent> physicsMapper =
      ComponentMapper.getFor(PhysicsComponent.class);

  public MeleeWeaponSystem() {
    super(
        Family.all(MeleeWeaponComponent.class, WeaponComponent.class, ParentComponent.class).get());
  }

  @Override
  protected void processEntity(Entity weaponEntity, float deltaTime) {
    WeaponComponent weapon = weaponMapper.get(weaponEntity);
    Entity owner = parentMapper.get(weaponEntity).parent;
    if (owner == null) {
      return;
    }

    if (!canAttackTarget(owner, weapon)) {
      resetAttackState(owner, weapon);
      return;
    }

    if (!attackingMapper.has(owner)) {
      startAttack(owner, weapon);
      return;
    }

    processAnimationEvents(owner, weapon);
  }

  private boolean canAttackTarget(Entity owner, WeaponComponent weapon) {
    if (weapon.targetEntity == null) {
      return false;
    }

    PositionComponent ownerPosition = positionMapper.get(owner);
    PositionComponent targetPosition = positionMapper.get(weapon.targetEntity);
    if (ownerPosition == null || targetPosition == null) {
      return false;
    }

    float dx = ownerPosition.x - targetPosition.x;
    float dy = ownerPosition.y - targetPosition.y;
    float centerToCenter = (float) Math.hypot(dx, dy);
    float ownerRadius = getBodyRadiusPixels(physicsMapper.get(owner));
    float targetRadius = getBodyRadiusPixels(physicsMapper.get(weapon.targetEntity));
    float edgeToEdge = Math.max(0f, centerToCenter - ownerRadius - targetRadius);
    return edgeToEdge <= weapon.attackRange;
  }

  private float getBodyRadiusPixels(PhysicsComponent physics) {
    if (physics == null || physics.body == null || physics.body.getFixtureList().size == 0) {
      return 0f;
    }
    Fixture fixture = physics.body.getFixtureList().first();
    if (fixture.getShape() instanceof CircleShape) {
      CircleShape circle = (CircleShape) fixture.getShape();
      return circle.getRadius() / PhysicsConstants.METERS_PER_PIXEL;
    }
    return 0f;
  }

  private void startAttack(Entity owner, WeaponComponent weapon) {
    weapon.isAttacking = true;
    weapon.hasDamagedThisAttack = false;
    owner.add(getEngine().createComponent(AttackingComponent.class));

    AnimEventComponent events = getOrCreateEvents(owner);
    events.eventFrames.clear();
    events.defineEvent(AnimEventType.HIT_FRAME, weapon.damageFrame);
  }

  private void processAnimationEvents(Entity owner, WeaponComponent weapon) {
    AnimEventComponent events = eventMapper.get(owner);
    if (events == null) {
      return;
    }

    if (events.hasEvent(AnimEventType.HIT_FRAME) && !weapon.hasDamagedThisAttack) {
      DamageComponent ownerDamage = damageMapper.get(owner);
      float appliedDamage = ownerDamage != null ? ownerDamage.damage : weapon.damage;
      PendingDamageComponent pending = getEngine().createComponent(PendingDamageComponent.class);
      pending.amount = appliedDamage;
      weapon.targetEntity.add(pending);
      weapon.hasDamagedThisAttack = true;
    }

    if (events.hasEvent(AnimEventType.END)) {
      resetAttackState(owner, weapon);
      events.eventFrames.clear();
    }
  }

  private void resetAttackState(Entity owner, WeaponComponent weapon) {
    weapon.isAttacking = false;
    weapon.hasDamagedThisAttack = false;
    if (attackingMapper.has(owner)) {
      owner.remove(AttackingComponent.class);
    }
  }

  private AnimEventComponent getOrCreateEvents(Entity owner) {
    AnimEventComponent events = eventMapper.get(owner);
    if (events != null) {
      return events;
    }

    events = getEngine().createComponent(AnimEventComponent.class);
    owner.add(events);
    return events;
  }
}
