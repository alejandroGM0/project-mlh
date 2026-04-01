package io.github.proyectoM.registry;

import com.badlogic.gdx.utils.JsonValue;
import io.github.proyectoM.templates.BulletTemplate;
import java.util.HashSet;
import java.util.Set;

/** Loads and exposes bullet templates from {@code data/bullets.json}. */
public final class BulletRegistry extends AbstractJsonRegistry<BulletTemplate> {
  private static final String BULLETS_JSON_PATH = "data/bullets.json";

  private static final BulletRegistry INSTANCE = new BulletRegistry();

  private BulletRegistry() {}

  public static BulletRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  protected String getJsonPath() {
    return BULLETS_JSON_PATH;
  }

  @Override
  protected String getId(BulletTemplate template) {
    return template.id;
  }

  @Override
  protected BulletTemplate readTemplate(JsonValue bulletNode) {
    BulletTemplate template = new BulletTemplate();
    template.id = bulletNode.getString("id");
    template.speed = bulletNode.getFloat("speed");
    template.sprite = bulletNode.getString("sprite");
    template.scale = bulletNode.getFloat("scale");
    template.maxdistance = bulletNode.getInt("maxdistance");
    return template;
  }

  /** Returns the set of all sprite paths referenced by loaded bullet templates. */
  public Set<String> getAllSpritePaths() {
    Set<String> spritePaths = new HashSet<>();
    for (BulletTemplate template : getAll().values()) {
      addIfPresent(spritePaths, template.sprite);
    }
    return spritePaths;
  }
}
