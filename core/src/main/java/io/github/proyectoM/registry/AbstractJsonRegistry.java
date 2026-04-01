package io.github.proyectoM.registry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for JSON-backed registries that parse data files into template objects keyed by ID.
 *
 * @param <T> the template type stored in this registry
 */
public abstract class AbstractJsonRegistry<T> {
  private Map<String, T> templates;
  private boolean loaded = false;

  /** Returns the internal-file path to the JSON data file. */
  protected abstract String getJsonPath();

  /** Extracts the unique identifier from a parsed template. */
  protected abstract String getId(T template);

  /** Deserializes a single template from the given JSON node. */
  protected abstract T readTemplate(JsonValue node);

  /**
   * Hook for subclasses to override the root node iteration. By default the root's children are
   * iterated directly.
   */
  protected JsonValue extractNodes(JsonValue root) {
    return root;
  }

  /** Hook for subclasses to validate or fix up a template after parsing. */
  protected void validateTemplate(T template) {}

  /** Returns an unmodifiable view of all loaded templates keyed by ID. */
  public Map<String, T> getAll() {
    ensureLoaded();
    return Collections.unmodifiableMap(templates);
  }

  /** Returns the template for the given ID, or {@code null} if not found. */
  public T getTemplate(String id) {
    ensureLoaded();
    return templates.get(id);
  }

  /** Adds the given value to the set if it is non-null and non-empty. */
  protected void addIfPresent(Set<String> set, String value) {
    if (value != null && !value.isEmpty()) {
      set.add(value);
    }
  }

  private void ensureLoaded() {
    if (!loaded) {
      load();
    }
  }

  /** Eagerly loads all templates from the JSON file. Safe to call multiple times. */
  public void load() {
    templates = new LinkedHashMap<>();

    FileHandle file = Gdx.files.internal(getJsonPath());
    if (!file.exists()) {
      Gdx.app.error(getClass().getSimpleName(), "Data file not found: " + getJsonPath());
      loaded = true;
      return;
    }

    JsonReader reader = new JsonReader();
    JsonValue root = reader.parse(file);
    JsonValue nodes = extractNodes(root);

    for (JsonValue node = nodes.child; node != null; node = node.next) {
      T template = readTemplate(node);
      validateTemplate(template);
      templates.put(getId(template), template);
    }

    loaded = true;
  }
}
