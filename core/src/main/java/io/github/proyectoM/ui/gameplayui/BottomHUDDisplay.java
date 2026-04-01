package io.github.proyectoM.ui.gameplayui;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import io.github.proyectoM.components.companion.CompanionComponent;
import io.github.proyectoM.components.entity.combat.HealthComponent;

/** A bottom bar that displays compact information for all companions. */
public class BottomHUDDisplay implements Disposable {
  private static final float BOTTOM_PADDING = 20f;
  private static final float COMPANION_GAP = 12f;
  private static final float COMPANION_ELEMENT_CELL_PADDING = 6f;
  private static final String COMPANION_BACKGROUND_DRAWABLE = "white";
  private static final Color COMPANION_BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.55f);

  private final Skin skin;
  private final Table rootTable;
  private final HorizontalGroup companionsGroup;

  /**
   * Constructor for the BottomHUDDisplay.
   *
   * @param stage The shared stage for rendering.
   * @param skin A previously loaded UI skin.
   */
  public BottomHUDDisplay(Stage stage, Skin skin) {
    this.skin = skin;

    this.rootTable = new Table();
    rootTable.setFillParent(true);
    rootTable.bottom().padBottom(BOTTOM_PADDING);

    this.companionsGroup = new HorizontalGroup();
    companionsGroup.space(COMPANION_GAP);
    companionsGroup.align(Align.center);

    rootTable.add(companionsGroup);
    stage.addActor(rootTable);
  }

  /**
   * Updates the display with the current companions from the engine.
   *
   * @param engine The ECS engine to get companions from.
   */
  public void update(Engine engine) {
    ImmutableArray<Entity> companions =
        engine.getEntitiesFor(Family.all(CompanionComponent.class, HealthComponent.class).get());

    if (companions.size() != companionsGroup.getChildren().size) {
      rebuildCompanionDisplays(companions);
    }

    updateCompanionDisplays();
  }

  /**
   * A debug helper to set the health of the first visible companion.
   *
   * @param current The current test health.
   * @param max The maximum test health.
   */
  public void debugSetFirstCompanionHealth(int current, int max) {
    if (companionsGroup.getChildren().size > 0) {
      getDisplayAt(0).setTestHealth(current, max);
    }
  }

  /**
   * Rebuilds the companion displays when the number of companions changes.
   *
   * @param companions The current list of companions.
   */
  private void rebuildCompanionDisplays(ImmutableArray<Entity> companions) {
    companionsGroup.clear();

    for (int i = 0; i < companions.size(); i++) {
      Entity companion = companions.get(i);
      CompanionMiniDisplay display = new CompanionMiniDisplay(companion, skin);
      display.setBackground(
          skin.newDrawable(COMPANION_BACKGROUND_DRAWABLE, COMPANION_BACKGROUND_COLOR));
      display.pad(COMPANION_ELEMENT_CELL_PADDING);

      companionsGroup.addActor(display);
    }
  }

  private void updateCompanionDisplays() {
    for (int i = 0; i < companionsGroup.getChildren().size; i++) {
      getDisplayAt(i).update();
    }
  }

  private CompanionMiniDisplay getDisplayAt(int index) {
    return (CompanionMiniDisplay) companionsGroup.getChildren().get(index);
  }

  /** Disposes of the resources used by this display. */
  @Override
  public void dispose() {
    rootTable.remove();
  }
}
