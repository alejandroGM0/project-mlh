package io.github.proyectoM.templates;

import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;

/** Mutable data template loaded from weapon registry JSON. */
public class WeaponTemplate {
  public static final float DEFAULT_ATTACK_RANGE = 100f;
  public static final float DEFAULT_TARGET_RANGE = 500f;
  public static final float DEFAULT_RELOAD_TIME = 1.5f;
  public static final float DEFAULT_DAMAGE = 10f;
  public static final String DEFAULT_VARIANT = "default";
  public static final String TYPE_RANGED = "ranged";
  public static final String TYPE_MELEE = "melee";
  public static final int DEFAULT_DAMAGE_FRAME = 15;
  public static final int DEFAULT_ATTACK_VARIANT = 1;

  public String id;
  public String type = TYPE_RANGED;
  public float damage = DEFAULT_DAMAGE;
  public float attackRange = DEFAULT_ATTACK_RANGE;
  public float targetRange = DEFAULT_TARGET_RANGE;
  public float attackSpeed;
  public int damageFrame = DEFAULT_DAMAGE_FRAME;
  public String bulletType = "";
  public String sound = "";
  public float reloadTime = DEFAULT_RELOAD_TIME;
  public String atlas = "";
  public String flashAtlas = "";
  public String variant = DEFAULT_VARIANT;
  public int attackVariant = DEFAULT_ATTACK_VARIANT;
  public Map<String, Vector2> muzzlePoints = new HashMap<>();
}
