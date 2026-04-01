package io.github.proyectoM.systems.physics;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import io.github.proyectoM.components.enemy.EnemyComponent;
import io.github.proyectoM.components.entity.combat.DamageComponent;
import io.github.proyectoM.components.entity.combat.PendingDamageComponent;
import io.github.proyectoM.components.entity.combat.PendingRemoveComponent;
import io.github.proyectoM.components.entity.weapon.BulletComponent;

/**
 * A global ContactListener to detect relevant collisions and mark damage. This listener is not an
 * ECS system but a bridge between the physics and the ECS logic. It is used to intercept collision
 * events and delegate the damage and entity removal logic to the corresponding systems.
 */
public class GameContactListener implements ContactListener {
  private final Engine engine;

  public GameContactListener(Engine engine) {
    this.engine = engine;
  }
  /**
   * Called when a contact between two fixtures begins. It detects relevant collisions and marks
   * pending damage if applicable.
   *
   * @param contact The Box2D contact information.
   */
  @Override
  public void beginContact(Contact contact) {
    Entity a = getEntityFromFixture(contact.getFixtureA());
    Entity b = getEntityFromFixture(contact.getFixtureB());

    if (a == null || b == null) {
      return;
    }

    if (isBullet(a) && isZombie(b)) {
      markPendingDamage(a, b);
    } else if (isBullet(b) && isZombie(a)) {
      markPendingDamage(b, a);
    }
  }

  /**
   * Called when a contact between two fixtures ends. This is not used in this listener.
   *
   * @param contact The Box2D contact information.
   */
  @Override
  public void endContact(Contact contact) {}

  /**
   * Called before Box2D resolves the contact. This is not used in this listener.
   *
   * @param contact The Box2D contact information.
   * @param oldManifold The previous state of the contact manifold.
   */
  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {}

  /**
   * Called after Box2D resolves the contact. This is not used in this listener.
   *
   * @param contact The Box2D contact information.
   * @param impulse The impulse applied during the contact resolution.
   */
  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {}

  /**
   * Extracts the entity associated with a Box2D fixture.
   *
   * @param fixture The Box2D fixture.
   * @return The associated entity or null if there is none.
   */
  private Entity getEntityFromFixture(Fixture fixture) {
    Object userData = fixture.getBody().getUserData();
    return (userData instanceof Entity) ? (Entity) userData : null;
  }

  /**
   * Determines if the entity is a bullet.
   *
   * @param entity The entity to check.
   * @return true if it has a BulletComponent, false otherwise.
   */
  private boolean isBullet(Entity entity) {
    return entity.getComponent(BulletComponent.class) != null;
  }

  /**
   * Determines if the entity is a zombie.
   *
   * @param entity The entity to check.
   * @return true if it has an EnemyComponent, false otherwise.
   */
  private boolean isZombie(Entity entity) {
    return entity.getComponent(EnemyComponent.class) != null;
  }

  /**
   * Marks pending damage on the zombie and marks the bullet for removal.
   *
   * @param bullet The bullet entity.
   * @param zombie The zombie entity.
   */
  private void markPendingDamage(Entity bullet, Entity zombie) {
    DamageComponent damageComponent = bullet.getComponent(DamageComponent.class);
    if (damageComponent != null) {
      PendingDamageComponent pending = engine.createComponent(PendingDamageComponent.class);
      pending.amount = damageComponent.damage;
      zombie.add(pending);
      bullet.add(engine.createComponent(PendingRemoveComponent.class));
    }
  }
}
