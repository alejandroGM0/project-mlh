package io.github.proyectoM.registry;

import com.badlogic.gdx.utils.JsonValue;
import io.github.proyectoM.templates.CharacterTemplate;
import java.util.Set;

/** Loads and exposes enemy templates from {@code data/enemies.json}. */
public final class EnemyRegistry extends AbstractJsonRegistry<CharacterTemplate> {
  private static final String ENEMIES_JSON_PATH = "data/enemies.json";
  private static final String ENEMY_TYPES_KEY = "zombie_types";
  private static final String DEFAULT_WEAPON_ID = "fists";
  private static final float DEFAULT_ATTACK_RANGE_MULTIPLIER = 1f;

  private static final EnemyRegistry INSTANCE = new EnemyRegistry();

  private EnemyRegistry() {}

  public static EnemyRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getJsonPath() {
    return ENEMIES_JSON_PATH;
  }

  @Override
  protected String getId(CharacterTemplate template) {
    return template.id;
  }

  @Override
  protected JsonValue extractNodes(JsonValue root) {
    JsonValue enemyArray = root.get(ENEMY_TYPES_KEY);
    return enemyArray != null ? enemyArray : root;
  }

  @Override
  protected CharacterTemplate readTemplate(JsonValue enemyNode) {
    CharacterTemplate template = new CharacterTemplate();
    template.id = enemyNode.getString("id");
    template.name = enemyNode.getString("name");
    template.health = enemyNode.getFloat("health");
    template.damage = enemyNode.getFloat("damage");
    template.speed = enemyNode.getFloat("speed");
    template.armor = enemyNode.getFloat("armor", CharacterTemplate.DEFAULT_ARMOR);
    template.mass = enemyNode.getFloat("mass", CharacterTemplate.DEFAULT_MASS);
    template.atlas_path = enemyNode.getString("atlas_path", null);
    template.attackRangeMultiplier =
        enemyNode.getFloat("attackRangeMultiplier", DEFAULT_ATTACK_RANGE_MULTIPLIER);
    template.attackCooldownTime =
        enemyNode.getFloat("attackCooldownTime", CharacterTemplate.DEFAULT_ATTACK_COOLDOWN_TIME);
    template.scorePoints = enemyNode.getInt("scorePoints", CharacterTemplate.DEFAULT_SCORE_POINTS);
    template.weaponId = enemyNode.getString("weaponId", DEFAULT_WEAPON_ID);
    template.dieVariant = enemyNode.getInt("dieVariant", CharacterTemplate.DEFAULT_DIE_VARIANT);
    return template;
  }

  /** Returns the IDs of all loaded enemy types. */
  public static Set<String> getAllZombieIds() {
    return INSTANCE.getAll().keySet();
  }

  @Override
  protected void collectAtlasPaths(CharacterTemplate template, Set<String> paths) {
    addIfPresent(paths, template.atlas_path);
  }
}
