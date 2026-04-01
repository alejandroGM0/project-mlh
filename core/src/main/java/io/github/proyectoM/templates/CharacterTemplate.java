package io.github.proyectoM.templates;

/** Mutable data template loaded from registry JSON for enemies and companions. */
public class CharacterTemplate {
  public static final float DEFAULT_ARMOR = 0f;
  public static final float DEFAULT_MASS = 1f;
  public static final float DEFAULT_ATTACK_COOLDOWN_TIME = 1f;
  public static final int DEFAULT_SCORE_POINTS = 0;
  public static final int DEFAULT_KILL_REWARD = 0;
  public static final int DEFAULT_DIE_VARIANT = 0;

  public String id;
  public String name;
  public String description;

  public float health;
  public float damage;
  public float speed;
  public float armor = DEFAULT_ARMOR;
  public float mass = DEFAULT_MASS;

  public float attackRangeMultiplier;
  public float attackCooldownTime = DEFAULT_ATTACK_COOLDOWN_TIME;

  public String weaponId;

  // Preserved for JSON compatibility with the existing data files.
  @SuppressWarnings("checkstyle:MemberName")
  public String atlas_path;

  public int scorePoints = DEFAULT_SCORE_POINTS;
  public int killReward = DEFAULT_KILL_REWARD;
  public int dieVariant = DEFAULT_DIE_VARIANT;
}
