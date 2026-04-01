package io.github.proyectoM.templates;

/** Mutable data template loaded from bullet registry JSON. */
public class BulletTemplate {
  public String id;
  public float speed;
  public String sprite;
  public float scale;

  // Preserved for JSON compatibility with existing content files.
  public float maxdistance;
}
