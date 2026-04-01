package io.github.proyectoM.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.proyectoM.localization.LocalizationManager;
import io.github.proyectoM.resources.Assets;
import io.github.proyectoM.ui.gameplayui.BottomHUDDisplay;
import io.github.proyectoM.ui.gameplayui.TopBarDisplay;

/** Coordinates gameplay UI displays rendered on the shared stage. */
public class GameUISystem extends EntitySystem {
  private static final String UI_SKIN_PATH = "skins/pruebaInterfaz/pruebaInterfaz.json";
  private static final String KEY_HINTS_KEY = "hud.key_hints";
  private static final float HINTS_ALPHA = 0.4f;
  private static final float HINTS_PADDING = 8f;

  private final Stage uiStage;
  private final TopBarDisplay topBarDisplay;
  private final BottomHUDDisplay bottomHudDisplay;

  public GameUISystem(Stage uiStage, Engine engine) {
    this.uiStage = uiStage;

    Skin skin = loadSkin();
    topBarDisplay = new TopBarDisplay(uiStage, engine, skin);
    bottomHudDisplay = new BottomHUDDisplay(uiStage, skin);
    createKeyHintsLabel(skin);
  }

  @Override
  public void update(float deltaTime) {
    topBarDisplay.update();
    bottomHudDisplay.update(getEngine());
    uiStage.act(deltaTime);
  }

  private void createKeyHintsLabel(Skin skin) {
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = skin.getFont("Silkscreen-Regular");
    style.fontColor = new Color(1f, 1f, 1f, HINTS_ALPHA);

    Label hintsLabel = new Label(LocalizationManager.getInstance().get(KEY_HINTS_KEY), style);

    Table hintsTable = new Table();
    hintsTable.setFillParent(true);
    hintsTable.bottom().left();
    hintsTable.add(hintsLabel).pad(HINTS_PADDING);

    uiStage.addActor(hintsTable);
  }

  private Skin loadSkin() {
    return Assets.getManager().get(UI_SKIN_PATH, Skin.class);
  }
}
