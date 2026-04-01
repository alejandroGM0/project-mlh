package io.github.proyectoM.ui.gameplayui;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.proyectoM.components.entity.AIComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;
import java.util.Locale;

/** A minimalist display for companion information for the bottom bar. */
public class CompanionMiniDisplay extends Table {
  private static final float COMPANION_NAME_FONT_SCALE = 0.85f;
  private static final float COMPANION_HEALTH_FONT_SCALE = 0.7f;
  private static final float COMPANION_INFO_ROW_PADDING = 6f;
  private static final String HEALTH_TEXT_FORMAT = "%d / %d";
  private static final String INFO_BACKGROUND_DRAWABLE = "white";
  private static final Color INFO_BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.55f);

  private final ComponentMapper<HealthComponent> healthMapper;
  private final ComponentMapper<AIComponent> aiMapper;

  private final Entity companion;
  private final Label nameLabel;
  private final Label healthValuesLabel;

  /**
   * Constructor for the CompanionMiniDisplay.
   *
   * @param companion The companion entity to display.
   * @param skin The default project skin.
   */
  public CompanionMiniDisplay(Entity companion, Skin skin) {
    super(skin);
    this.companion = companion;

    this.healthMapper = ComponentMapper.getFor(HealthComponent.class);
    this.aiMapper = ComponentMapper.getFor(AIComponent.class);

    AIComponent ai = requireAiComponent();
    HealthComponent health = requireHealthComponent();

    this.nameLabel = new Label(ai.name.toUpperCase(Locale.ROOT), skin);
    this.nameLabel.setFontScale(COMPANION_NAME_FONT_SCALE);

    this.healthValuesLabel = new Label(formatHealthText(health), skin);
    this.healthValuesLabel.setFontScale(COMPANION_HEALTH_FONT_SCALE);

    Table infoTable = new Table();
    infoTable.add(nameLabel).left().row();
    Table hpRow = new Table();

    hpRow.setBackground(skin.newDrawable(INFO_BACKGROUND_DRAWABLE, INFO_BACKGROUND_COLOR));
    hpRow.pad(COMPANION_INFO_ROW_PADDING);
    hpRow.add(healthValuesLabel).left();

    infoTable.add(hpRow).left();
    add(infoTable);
  }

  /** Updates the display values (HP, etc.). */
  public void update() {
    healthValuesLabel.setText(formatHealthText(requireHealthComponent()));
  }

  /**
   * Formats the text that displays the current and maximum HP.
   *
   * @param health The component with health information.
   * @return Text with the format "current / maximum".
   */
  private String formatHealthText(HealthComponent health) {
    return String.format(Locale.ROOT, HEALTH_TEXT_FORMAT, health.currentHealth, health.maxHealth);
  }

  /**
   * Sets health values for quick tests and refreshes the UI.
   *
   * @param current The current HP to force.
   * @param max The new maximum health.
   */
  public void setTestHealth(int current, int max) {
    HealthComponent health = requireHealthComponent();
    health.maxHealth = max;
    health.currentHealth = Math.max(0, Math.min(current, max));

    healthValuesLabel.setText(formatHealthText(health));
  }

  private AIComponent requireAiComponent() {
    AIComponent ai = aiMapper.get(companion);
    if (ai == null) {
      throw new IllegalStateException("CompanionMiniDisplay requires AIComponent.");
    }
    return ai;
  }

  private HealthComponent requireHealthComponent() {
    HealthComponent health = healthMapper.get(companion);
    if (health == null) {
      throw new IllegalStateException("CompanionMiniDisplay requires HealthComponent.");
    }
    return health;
  }
}
