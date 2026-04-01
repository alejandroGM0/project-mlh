package io.github.proyectoM.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Debug panel with checkboxes for toggling render-debug overlays at runtime. */
public class RenderDebugPanel implements DebugPanel {

  private static final String TITLE = "Render";

  private static final String SECTION_HEADER = "Overlays";
  private static final String PHYSICS_LABEL = " Physics (Box2D)";
  private static final String LIGHTING_LABEL = " Lighting (RayHandler)";
  private static final String LEADER_MARKER_LABEL = " Leader Marker";

  private static final float CELL_PAD = 5f;

  private static final Color SECTION_HEADER_COLOR = new Color(0.3f, 0.9f, 1f, 1f);

  private final RenderDebugSettings settings;

  private boolean active;

  /**
   * Creates the render debug panel.
   *
   * @param settings shared mutable settings that the render coordinator also reads
   */
  public RenderDebugPanel(RenderDebugSettings settings) {
    this.settings = settings;
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public void update(float delta) {}

  /** Builds a simple table of checkboxes, one per render overlay. */
  @Override
  public Actor buildPanel(Skin skin) {
    Table table = new Table(skin);
    table.top().left();
    table.defaults().pad(CELL_PAD).left();

    Label header = new Label(SECTION_HEADER, skin);
    header.setColor(SECTION_HEADER_COLOR);
    table.add(header).left().row();

    table
        .add(
            createToggle(
                skin,
                PHYSICS_LABEL,
                settings.physicsEnabled,
                checked -> settings.physicsEnabled = checked))
        .left()
        .row();
    table
        .add(
            createToggle(
                skin,
                LIGHTING_LABEL,
                settings.lightingEnabled,
                checked -> settings.lightingEnabled = checked))
        .left()
        .row();
    table
        .add(
            createToggle(
                skin,
                LEADER_MARKER_LABEL,
                settings.leaderMarkerEnabled,
                checked -> settings.leaderMarkerEnabled = checked))
        .left()
        .row();

    return table;
  }

  /**
   * Creates a checkbox wired to a boolean consumer.
   *
   * @param skin the UI skin
   * @param label the checkbox label
   * @param initialValue the initial checked state
   * @param onChange callback receiving the new checked state
   * @return the configured checkbox
   */
  private CheckBox createToggle(
      Skin skin, String label, boolean initialValue, BooleanConsumer onChange) {
    CheckBox checkBox = new CheckBox(label, skin);
    checkBox.setChecked(initialValue);
    checkBox.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            onChange.accept(checkBox.isChecked());
          }
        });
    return checkBox;
  }

  /**
   * Functional interface for a boolean consumer since java.util.function is not available in all
   * targets.
   */
  @FunctionalInterface
  private interface BooleanConsumer {

    /**
     * Accepts a boolean value.
     *
     * @param value the value
     */
    void accept(boolean value);
  }
}
