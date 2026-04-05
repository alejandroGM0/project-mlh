package io.github.proyectoM.registry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Collections;
import java.util.HashSet;
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

  /**
   * Returns the template for the given ID, throwing if not found.
   *
   * @param id the template identifier
   * @return the template, never null
   * @throws IllegalArgumentException if no template exists for the given ID
   */
  public T getRequired(String id) {
    T template = getTemplate(id);
    if (template == null) {
      throw new IllegalArgumentException(
          getClass().getSimpleName() + ": unknown template id '" + id + "'");
    }
    return template;
  }

  /** Adds the given value to the set if it is non-null and non-empty. */
  protected void addIfPresent(Set<String> set, String value) {
    if (value != null && !value.isEmpty()) {
      set.add(value);
    }
  }

  /**
   * Collects atlas paths from a single template into the given set. Override in subclasses to
   * extract registry-specific atlas fields using {@link #addIfPresent}.
   *
   * @param template the template to extract paths from
   * @param paths accumulator set for discovered atlas paths
   */
  protected void collectAtlasPaths(T template, Set<String> paths) {}

  /**
   * Returns all atlas paths across every loaded template.
   *
   * @return a mutable set of discovered atlas paths
   */
  public Set<String> getAllAtlasPaths() {
    Set<String> paths = new HashSet<>();
    for (T template : getAll().values()) {
      collectAtlasPaths(template, paths);
    }
    return paths;
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
      if (template == null) {
        throw new IllegalStateException(
            getClass().getSimpleName() + ": failed to parse node '" + node.name + "' in " + getJsonPath());
      }
      validateTemplate(template);
      templates.put(getId(template), template);
    }

    loaded = true;
  }
}
