package io.github.proyectoM.ui.gameplayui;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import io.github.proyectoM.components.game.GameStateComponent;
import io.github.proyectoM.components.game.ScoreComponent;
import java.util.Locale;

/** Displays the top UI bar with information about time, level, gold, and kills. */
public class TopBarDisplay implements Disposable {
  private static final String ELEMENT_BACKGROUND_DRAWABLE = "white";
  private static final String LVL_LABEL = "LVL";
  private static final String XP_SEPARATOR = "/";
  private static final String KILLS_SUFFIX = " KILLS";
  private static final String GOLD_SUFFIX = " GOLD";
  private static final String TIME_FORMAT = "%02d:%02d";
  private static final String INITIAL_TIME_TEXT = "00:00";
  private static final String INITIAL_KILLS_TEXT = "0" + KILLS_SUFFIX;
  private static final String INITIAL_GOLD_TEXT = "0" + GOLD_SUFFIX;

  private static final int TEMP_LEVEL = 12;
  private static final int TEMP_XP_CURRENT = 2450;
  private static final int TEMP_XP_MAX = 3000;

  private static final long MILLISECONDS_PER_SECOND = 1000L;
  private static final int SECONDS_PER_MINUTE = 60;
  private static final float SIDE_PADDING = 24f;
  private static final float TOP_PADDING = 10f;
  private static final float TOPBAR_ELEMENT_PAD = 6f;
  private static final float LEVEL_LABEL_RIGHT_PADDING = 8f;
  private static final float LEVEL_NUMBER_RIGHT_PADDING = 16f;
  private static final float GOLD_RIGHT_PADDING = 30f;

  private static final Color COLOR_TEXT_GREY = new Color(0.6f, 0.6f, 0.6f, 1f);
  private static final Color COLOR_TEXT_GREEN = new Color(0f, 1f, 0f, 1f);
  private static final Color COLOR_TEXT_LIGHT_GREY = new Color(0.75f, 0.75f, 0.75f, 1f);
  private static final Color COLOR_GOLD = new Color(1f, 0.84f, 0f, 1f);
  private static final Color ELEMENT_BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.45f);

  private final long startTimeMillis;

  private final Table rootTable;
  private final Label lvlLabel;
  private final Label levelNumberLabel;
  private final Label xpLabel;
  private final Label timeLabel;
  private final Label killsLabel;
  private final Label goldLabel;

  private final Engine engine;
  private final ComponentMapper<ScoreComponent> scoreMapper;
  private Entity globalStateEntity;

  /**
   * Constructor for the TopBarDisplay.
   *
   * @param stage The shared stage for rendering.
   * @param engine The entity engine to access components.
   * @param skin A previously loaded UI skin.
   */
  public TopBarDisplay(Stage stage, Engine engine, Skin skin) {
    this.engine = engine;
    this.scoreMapper = ComponentMapper.getFor(ScoreComponent.class);

    this.startTimeMillis = System.currentTimeMillis();

    Label.LabelStyle styleSilkscreen = new Label.LabelStyle();
    styleSilkscreen.font = skin.getFont("Silkscreen-Regular");

    Label.LabelStyle styleClock = new Label.LabelStyle();
    styleClock.font = skin.getFont("Silkscreen-Bold");

    this.rootTable = new Table();
    this.rootTable.setFillParent(true);
    this.rootTable.top();
    this.rootTable.padTop(TOP_PADDING);
    this.rootTable.padLeft(SIDE_PADDING);
    this.rootTable.padRight(SIDE_PADDING);

    Table leftSection = new Table();
    this.lvlLabel = new Label(LVL_LABEL, styleSilkscreen);
    this.lvlLabel.setColor(COLOR_TEXT_GREY);
    this.levelNumberLabel = new Label(String.valueOf(TEMP_LEVEL), styleSilkscreen);
    this.levelNumberLabel.setColor(COLOR_TEXT_GREEN);
    this.xpLabel = new Label(TEMP_XP_CURRENT + XP_SEPARATOR + TEMP_XP_MAX, styleSilkscreen);
    this.xpLabel.setColor(COLOR_TEXT_LIGHT_GREY);

    Table lvlCell = createBackgroundCell(skin, lvlLabel);
    Table levelNumCell = createBackgroundCell(skin, levelNumberLabel);
    Table xpCell = createBackgroundCell(skin, xpLabel);

    leftSection.add(lvlCell).padRight(LEVEL_LABEL_RIGHT_PADDING);
    leftSection.add(levelNumCell).padRight(LEVEL_NUMBER_RIGHT_PADDING);
    leftSection.add(xpCell);

    Table centerSection = new Table();
    this.timeLabel = new Label(INITIAL_TIME_TEXT, styleClock);
    this.timeLabel.setColor(COLOR_TEXT_GREEN);
    Table timeCell = createBackgroundCell(skin, timeLabel);
    centerSection.add(timeCell).pad(TOPBAR_ELEMENT_PAD);

    Table rightSection = new Table();
    this.killsLabel = new Label(INITIAL_KILLS_TEXT, styleSilkscreen);
    this.killsLabel.setColor(COLOR_TEXT_LIGHT_GREY);
    this.goldLabel = new Label(INITIAL_GOLD_TEXT, styleSilkscreen);
    this.goldLabel.setColor(COLOR_GOLD);

    Table goldCell = createBackgroundCell(skin, goldLabel);
    Table killsCell = createBackgroundCell(skin, killsLabel);

    rightSection.add(goldCell).padRight(GOLD_RIGHT_PADDING);
    rightSection.add(killsCell);

    rootTable.add(leftSection).expandX().left();
    rootTable.add(centerSection).expandX().center();
    rootTable.add(rightSection).expandX().right();

    stage.addActor(rootTable);
  }

  /** Updates the display. */
  public void update() {
    updateScoreLabels();
    updateTimeLabel();
  }

  private void updateScoreLabels() {
    if (globalStateEntity == null) {
      ImmutableArray<Entity> entities =
          engine.getEntitiesFor(Family.all(GameStateComponent.class).get());
      if (entities.size() > 0) {
        globalStateEntity = entities.first();
      }
    }

    if (globalStateEntity != null) {
      ScoreComponent scoreComponent = scoreMapper.get(globalStateEntity);
      if (scoreComponent != null) {
        goldLabel.setText(scoreComponent.score + GOLD_SUFFIX);
        killsLabel.setText(scoreComponent.enemiesKilled + KILLS_SUFFIX);
      }
    }
  }

  private void updateTimeLabel() {
    long elapsedSeconds = (System.currentTimeMillis() - startTimeMillis) / MILLISECONDS_PER_SECOND;
    int minutes = (int) (elapsedSeconds / SECONDS_PER_MINUTE);
    int seconds = (int) (elapsedSeconds % SECONDS_PER_MINUTE);
    String timeText = String.format(Locale.ROOT, TIME_FORMAT, minutes, seconds);
    timeLabel.setText(timeText);
  }

  private Table createBackgroundCell(Skin skin, Label label) {
    Table cell = new Table(skin);
    cell.setBackground(skin.newDrawable(ELEMENT_BACKGROUND_DRAWABLE, ELEMENT_BACKGROUND_COLOR));
    cell.add(label).pad(TOPBAR_ELEMENT_PAD);
    return cell;
  }

  /** Disposes of the resources used by this display. */
  @Override
  public void dispose() {
    rootTable.remove();
  }
}
