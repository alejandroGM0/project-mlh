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
import io.github.proyectoM.components.entity.weapon.WeaponStateComponent;
import io.github.proyectoM.components.entity.weapon.types.MeleeWeaponComponent;
import io.github.proyectoM.physics.PhysicsConstants;

/** Drives melee weapon attacks using animation events for hit timing and completion. */
public class MeleeWeaponSystem extends IteratingSystem {
  private final ComponentMapper<WeaponComponent> weaponMapper =
      ComponentMapper.getFor(WeaponComponent.class);
  private final ComponentMapper<WeaponStateComponent> weaponStateMapper =
      ComponentMapper.getFor(WeaponStateComponent.class);
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
    WeaponStateComponent weaponState = weaponStateMapper.get(weaponEntity);
    Entity owner = parentMapper.get(weaponEntity).parent;

    if (!canAttackTarget(owner, weapon, weaponState)) {
      resetAttackState(owner, weaponState);
      return;
    }

    if (!attackingMapper.has(owner)) {
      startAttack(owner, weapon, weaponState);
      return;
    }

    processAnimationEvents(owner, weapon, weaponState);
  }

  private boolean canAttackTarget(Entity owner, WeaponComponent weapon, WeaponStateComponent weaponState) {
    if (weaponState.targetEntity == null) {
      return false;
    }

    PositionComponent ownerPosition = positionMapper.get(owner);
    PositionComponent targetPosition = positionMapper.get(weaponState.targetEntity);

    if (ownerPosition == null || targetPosition == null) {
      return false;
    }

    float dx = ownerPosition.x - targetPosition.x;
    float dy = ownerPosition.y - targetPosition.y;
    float centerToCenter = (float) Math.hypot(dx, dy);
    float ownerRadius = getBodyRadiusPixels(physicsMapper.get(owner));
    float targetRadius = getBodyRadiusPixels(physicsMapper.get(weaponState.targetEntity));
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

  private void startAttack(Entity owner, WeaponComponent weapon, WeaponStateComponent weaponState) {
    weaponState.isAttacking = true;
    weaponState.hasDamagedThisAttack = false;
    owner.add(getEngine().createComponent(AttackingComponent.class));

    AnimEventComponent events = getOrCreateEvents(owner);
    events.eventFrames.clear();
    events.defineEvent(AnimEventType.HIT_FRAME, weapon.damageFrame);
  }

  private void processAnimationEvents(Entity owner, WeaponComponent weapon, WeaponStateComponent weaponState) {
    AnimEventComponent events = eventMapper.get(owner);
    if (events == null) {
      return;
    }

    if (events.hasEvent(AnimEventType.HIT_FRAME) && !weaponState.hasDamagedThisAttack) {
      float appliedDamage = damageMapper.get(owner).damage;
      PendingDamageComponent pending = getEngine().createComponent(PendingDamageComponent.class);
      pending.amount = appliedDamage;
      weaponState.targetEntity.add(pending);
      weaponState.hasDamagedThisAttack = true;
    }

    if (events.hasEvent(AnimEventType.END)) {
      resetAttackState(owner, weaponState);
      events.eventFrames.clear();
    }
  }

  private void resetAttackState(Entity owner, WeaponStateComponent weaponState) {
    weaponState.isAttacking = false;
    weaponState.hasDamagedThisAttack = false;
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
