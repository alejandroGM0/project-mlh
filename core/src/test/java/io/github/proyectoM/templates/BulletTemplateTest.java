package io.github.proyectoM.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BulletTemplateTest {

  /**
   * Verifies that a newly created BulletTemplate has default fields.
   */
  @Test
  void newTemplate_hasNullAndZeroDefaults() {
    BulletTemplate template = new BulletTemplate();

    assertNull(template.id);
    assertEquals(0f, template.speed);
    assertNull(template.sprite);
    assertEquals(0f, template.scale);
    assertEquals(0f, template.maxdistance);
  }

  /**
   * Verifies field assignment simulating a bullet from JSON.
   */
  @Test
  void template_canBePopulatedLikeJson() {
    BulletTemplate template = new BulletTemplate();

    template.id = "player_bullet";
    template.speed = 900f;
    template.sprite = "weapons/bullets/bullet2.png";
    template.scale = 0.02f;
    template.maxdistance = 800f;

    assertEquals("player_bullet", template.id);
    assertEquals(900f, template.speed);
    assertEquals("weapons/bullets/bullet2.png", template.sprite);
    assertEquals(0.02f, template.scale);
    assertEquals(800f, template.maxdistance);
  }

  /**
   * Verifies that two templates can have distinct values simultaneously.
   */
  @Test
  void twoTemplates_haveIndependentValues() {
    BulletTemplate fast = new BulletTemplate();
    fast.id = "fast_bullet";
    fast.speed = 1400f;

    BulletTemplate slow = new BulletTemplate();
    slow.id = "heavy_bullet";
    slow.speed = 800f;

    assertEquals("fast_bullet", fast.id);
    assertEquals("heavy_bullet", slow.id);
    assertEquals(1400f, fast.speed);
    assertEquals(800f, slow.speed);
  }
}