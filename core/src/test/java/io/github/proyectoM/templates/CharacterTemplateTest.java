package io.github.proyectoM.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CharacterTemplateTest {

  /**
   * Verifies that a newly created CharacterTemplate has the correct default values.
   */
  @Test
  void newTemplate_hasCorrectDefaults() {
    CharacterTemplate template = new CharacterTemplate();

    assertEquals(CharacterTemplate.DEFAULT_ARMOR, template.armor);
    assertEquals(CharacterTemplate.DEFAULT_MASS, template.mass);
    assertEquals(CharacterTemplate.DEFAULT_ATTACK_COOLDOWN_TIME, template.attackCooldownTime);
    assertEquals(CharacterTemplate.DEFAULT_SCORE_POINTS, template.scorePoints);
    assertEquals(CharacterTemplate.DEFAULT_KILL_REWARD, template.killReward);
    assertEquals(CharacterTemplate.DEFAULT_DIE_VARIANT, template.dieVariant);
  }

  /**
   * Verifies that text fields are null by default.
   */
  @Test
  void newTemplate_stringFieldsAreNull() {
    CharacterTemplate template = new CharacterTemplate();

    assertNull(template.id);
    assertNull(template.name);
    assertNull(template.description);
    assertNull(template.weaponId);
    assertNull(template.atlas_path);
  }

  /**
   * Verifies that primitive numeric fields are 0 by default.
   */
  @Test
  void newTemplate_numericFieldsDefaultToZero() {
    CharacterTemplate template = new CharacterTemplate();

    assertEquals(0f, template.health);
    assertEquals(0f, template.damage);
    assertEquals(0f, template.speed);
    assertEquals(0f, template.attackRangeMultiplier);
  }

  /**
   * Verifies that default value constants have the expected values.
   */
  @Test
  void defaultConstants_haveExpectedValues() {
    assertEquals(0f, CharacterTemplate.DEFAULT_ARMOR);
    assertEquals(1f, CharacterTemplate.DEFAULT_MASS);
    assertEquals(1f, CharacterTemplate.DEFAULT_ATTACK_COOLDOWN_TIME);
    assertEquals(0, CharacterTemplate.DEFAULT_SCORE_POINTS);
    assertEquals(0, CharacterTemplate.DEFAULT_KILL_REWARD);
    assertEquals(0, CharacterTemplate.DEFAULT_DIE_VARIANT);
  }

  /**
   * Verifies full template assignment simulating an enemy.
   */
  @Test
  void template_canBePopulatedAsEnemy() {
    CharacterTemplate template = new CharacterTemplate();

    template.id = "zombie_basic";
    template.name = "Basic Zombie";
    template.health = 100f;
    template.damage = 10f;
    template.speed = 5f;
    template.armor = 0f;
    template.mass = 1f;
    template.weaponId = "fists";
    template.atlas_path = "characters/zombie/zombie.atlas";
    template.scorePoints = 10;

    assertEquals("zombie_basic", template.id);
    assertEquals("Basic Zombie", template.name);
    assertEquals(100f, template.health);
    assertEquals(10f, template.damage);
    assertEquals("fists", template.weaponId);
  }

  /**
   * Verifies full template assignment simulating a companion.
   */
  @Test
  void template_canBePopulatedAsCompanion() {
    CharacterTemplate template = new CharacterTemplate();

    template.id = "soldier";
    template.name = "Soldier";
    template.description = "Basic companion";
    template.health = 200f;
    template.damage = 20f;
    template.speed = 6f;
    template.weaponId = "rifle";
    template.killReward = 5;

    assertEquals("soldier", template.id);
    assertEquals("rifle", template.weaponId);
    assertEquals(5, template.killReward);
  }
}