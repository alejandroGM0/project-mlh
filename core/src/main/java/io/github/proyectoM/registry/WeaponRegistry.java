package io.github.proyectoM.registry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.proyectoM.templates.WeaponTemplate;
import java.util.HashSet;
import java.util.Set;

/** Loads and exposes weapon templates from {@code data/weapons.json}. */
public final class WeaponRegistry extends AbstractJsonRegistry<WeaponTemplate> {
  private static final String WEAPONS_JSON_PATH = "data/weapons.json";
  private static final String ATLAS_SUFFIX = ".atlas";
  private static final String MUZZLE_SUFFIX = ".muzzles.json";
  private static final float DEFAULT_MUZZLE_COORDINATE = 0f;
  private static final float DEFAULT_ATTACK_RANGE_FALLBACK = 300f;

  private static final WeaponRegistry INSTANCE = new WeaponRegistry();

  private WeaponRegistry() {}

  public static WeaponRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getJsonPath() {
    return WEAPONS_JSON_PATH;
  }

  @Override
  protected String getId(WeaponTemplate template) {
    return template.id;
  }

  @Override
  protected WeaponTemplate readTemplate(JsonValue weaponNode) {
    WeaponTemplate template = new WeaponTemplate();
    template.id = weaponNode.getString("id");
    template.type = weaponNode.getString("type", WeaponTemplate.TYPE_RANGED);
    template.attackRange = weaponNode.getFloat("attackRange", WeaponTemplate.DEFAULT_ATTACK_RANGE);
    template.targetRange = weaponNode.getFloat("targetRange", WeaponTemplate.DEFAULT_TARGET_RANGE);
    template.attackSpeed = weaponNode.getFloat("attackSpeed");
    template.damageFrame = weaponNode.getInt("damageFrame", WeaponTemplate.DEFAULT_DAMAGE_FRAME);
    template.bulletType = weaponNode.getString("bulletType", null);
    template.sound = weaponNode.getString("sound", null);
    template.atlas = weaponNode.getString("atlas_path", null);
    template.flashAtlas = weaponNode.getString("flash_atlas", null);
    template.variant = weaponNode.getString("variant", WeaponTemplate.DEFAULT_VARIANT);
    template.attackVariant =
        weaponNode.getInt("attackVariant", WeaponTemplate.DEFAULT_ATTACK_VARIANT);
    template.damage = weaponNode.getFloat("damage", WeaponTemplate.DEFAULT_DAMAGE);

    loadMuzzlePoints(template);
    return template;
  }

  /** Returns the attack range for the given weapon, or a default fallback if not found. */
  public float getWeaponAttackRange(String weaponId) {
    WeaponTemplate template = getTemplate(weaponId);
    return template != null ? template.attackRange : DEFAULT_ATTACK_RANGE_FALLBACK;
  }

  /** Returns the set of all atlas paths referenced by loaded weapon templates. */
  public Set<String> getAllAtlasPaths() {
    Set<String> atlasPaths = new HashSet<>();
    for (WeaponTemplate template : getAll().values()) {
      addIfPresent(atlasPaths, template.atlas);
      addIfPresent(atlasPaths, template.flashAtlas);
    }
    return atlasPaths;
  }

  private static void loadMuzzlePoints(WeaponTemplate template) {
    if (template.atlas == null || template.atlas.isEmpty()) {
      return;
    }

    String muzzlePath = template.atlas.replace(ATLAS_SUFFIX, MUZZLE_SUFFIX);
    FileHandle muzzleFile = Gdx.files.internal(muzzlePath);
    if (!muzzleFile.exists()) {
      return;
    }

    JsonReader reader = new JsonReader();
    JsonValue root = reader.parse(muzzleFile);
    for (JsonValue entry = root.child; entry != null; entry = entry.next) {
      String frameName = entry.name;
      if (frameName == null) {
        continue;
      }
      float x = entry.getFloat("x", DEFAULT_MUZZLE_COORDINATE);
      float y = entry.getFloat("y", DEFAULT_MUZZLE_COORDINATE);
      template.muzzlePoints.put(frameName, new Vector2(x, y));
    }
  }
}
