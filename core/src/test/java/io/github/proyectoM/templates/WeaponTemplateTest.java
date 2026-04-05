package io.github.proyectoM.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WeaponTemplateTest {

  /**
   * Verifies that a newly created WeaponTemplate has the correct default values.
   */
  @Test
  void newTemplate_hasCorrectDefaults() {
    WeaponTemplate template = new WeaponTemplate();

    assertEquals(WeaponTemplate.TYPE_RANGED, template.type);
    assertEquals(WeaponTemplate.DEFAULT_DAMAGE, template.damage);
    assertEquals(WeaponTemplate.DEFAULT_ATTACK_RANGE, template.attackRange);
    assertEquals(WeaponTemplate.DEFAULT_TARGET_RANGE, template.targetRange);
    assertEquals(WeaponTemplate.DEFAULT_DAMAGE_FRAME, template.damageFrame);
    assertEquals(WeaponTemplate.DEFAULT_RELOAD_TIME, template.reloadTime);
    assertEquals(WeaponTemplate.DEFAULT_VARIANT, template.variant);
    assertEquals(WeaponTemplate.DEFAULT_ATTACK_VARIANT, template.attackVariant);
  }

  /**
   * Verifies that the muzzle points map is initialized but empty.
   */
  @Test
  void newTemplate_muzzlePointsInitialized() {
    WeaponTemplate template = new WeaponTemplate();

    assertNotNull(template.muzzlePoints);
    assertTrue(template.muzzlePoints.isEmpty());
  }

  /**
   * Verifies that the weapon type constants are defined.
   */
  @Test
  void typeConstants_areDefined() {
    assertEquals("ranged", WeaponTemplate.TYPE_RANGED);
    assertEquals("melee", WeaponTemplate.TYPE_MELEE);
  }

  /**
   * Verifies that the template fields are assignable.
   */
  @Test
  void template_fieldsAreAssignable() {
    WeaponTemplate template = new WeaponTemplate();

    template.id = "test_weapon";
    template.type = WeaponTemplate.TYPE_MELEE;
    template.damage = 50f;
    template.attackRange = 200f;
    template.attackSpeed = 0.5f;
    template.bulletType = "test_bullet";

    assertEquals("test_weapon", template.id);
    assertEquals(WeaponTemplate.TYPE_MELEE, template.type);
    assertEquals(50f, template.damage);
    assertEquals(200f, template.attackRange);
    assertEquals(0.5f, template.attackSpeed);
    assertEquals("test_bullet", template.bulletType);
  }
}