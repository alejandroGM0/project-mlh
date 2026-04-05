package io.github.proyectoM.registry;

import com.badlogic.gdx.utils.JsonValue;
import io.github.proyectoM.templates.CharacterTemplate;
import java.util.Set;

/** Loads and exposes companion templates from {@code data/companions.json}. */
public final class CompanionRegistry extends AbstractJsonRegistry<CharacterTemplate> {
  private static final String COMPANIONS_JSON_PATH = "data/companions.json";
  private static final String DEFAULT_DESCRIPTION = "";
  private static final String DEFAULT_WEAPON_ID = "pistol";
  private static final float DEFAULT_ATTACK_RANGE_MULTIPLIER = 0.9f;
  private static final float DEFAULT_ATTACK_COOLDOWN = 1.5f;

  private static final CompanionRegistry INSTANCE = new CompanionRegistry();

  private CompanionRegistry() {}

  public static CompanionRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getJsonPath() {
    return COMPANIONS_JSON_PATH;
  }

  @Override
  protected String getId(CharacterTemplate template) {
    return template.id;
  }

  @Override
  protected void validateTemplate(CharacterTemplate template) {
    if (template.name == null || template.name.isEmpty()) {
      template.name = template.id;
    }
    if (template.weaponId == null || template.weaponId.isEmpty()) {
      template.weaponId = DEFAULT_WEAPON_ID;
    }
  }

  @Override
  protected CharacterTemplate readTemplate(JsonValue companionNode) {
    CharacterTemplate template = new CharacterTemplate();
    template.id = companionNode.getString("id");
    template.name = companionNode.getString("name");
    template.description = companionNode.getString("description", DEFAULT_DESCRIPTION);
    template.health = companionNode.getFloat("health");
    template.damage = companionNode.getFloat("damage");
    template.speed = companionNode.getFloat("speed");
    template.armor = companionNode.getFloat("armor", CharacterTemplate.DEFAULT_ARMOR);
    template.mass = companionNode.getFloat("mass", CharacterTemplate.DEFAULT_MASS);
    template.attackRangeMultiplier =
        companionNode.getFloat("attackRangeMultiplier", DEFAULT_ATTACK_RANGE_MULTIPLIER);
    template.attackCooldownTime =
        companionNode.getFloat("attackCooldownTime", DEFAULT_ATTACK_COOLDOWN);
    template.weaponId = companionNode.getString("weaponId", DEFAULT_WEAPON_ID);
    template.atlas_path = companionNode.getString("atlas_path", null);
    template.killReward = companionNode.getInt("killReward", CharacterTemplate.DEFAULT_KILL_REWARD);
    return template;
  }

  /** Returns the IDs of all loaded companion types. */
  public static Set<String> getAllCompanionIds() {
    return INSTANCE.getAll().keySet();
  }

  @Override
  protected void collectAtlasPaths(CharacterTemplate template, Set<String> paths) {
    addIfPresent(paths, template.atlas_path);
  }
}
