package io.github.proyectoM.debug;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.proyectoM.ecs.ProfiledEngine;
import java.util.Locale;
import java.util.Objects;

/** Debug panel that summarizes ECS timing data gathered by {@link ProfiledEngine}. */
public final class EcsMetricsPanel implements DebugPanel {
  private static final String TITLE = "ECS Metrics";
  private static final String SYSTEM_HEADER = "System";
  private static final String TIME_HEADER = "Time";
  private static final String LOAD_HEADER = "Load";
  private static final String RESET_PEAKS_LABEL = "Reset Peaks";
  private static final String ENGINE_TIME_TEMPLATE = "Engine: %.2f ms";
  private static final String SYSTEM_TIME_TEMPLATE = "%.2fms";
  private static final String ENTITIES_TEMPLATE = "Entities: %d";
  private static final String SYSTEMS_TEMPLATE = "Systems: %d";
  private static final String ENGINE_PLACEHOLDER = "Engine: -- ms";
  private static final String ENTITIES_PLACEHOLDER = "Entities: --";
  private static final String SYSTEMS_PLACEHOLDER = "Systems: --";
  private static final String SYSTEM_SUFFIX = "System";
  private static final String NAME_ELLIPSIS = "...";

  private static final int MAX_SYSTEMS_DISPLAY = 15;
  private static final int NAME_COLUMN_WIDTH = 180;
  private static final int TIME_COLUMN_WIDTH = 70;
  private static final int BAR_WIDTH = 100;
  private static final int BAR_HEIGHT = 12;
  private static final int MAIN_TABLE_PADDING = 5;
  private static final int SECTION_SPACING = 8;
  private static final int MAX_VISIBLE_NAME_LENGTH = 20;
  private static final int TRUNCATED_NAME_LENGTH = 17;

  private static final float UPDATE_INTERVAL = 0.5f;
  private static final float SMOOTHING_FACTOR = 0.3f;
  private static final float GOOD_TIME_THRESHOLD_MS = 0.3f;
  private static final float WARNING_TIME_THRESHOLD_MS = 1.0f;
  private static final float PROGRESS_BAR_MIN = 0f;
  private static final float PROGRESS_BAR_MAX = 1f;
  private static final float PROGRESS_BAR_STEP = 0.01f;
  private static final float ZERO_TIME_MS = 0f;

  private static final Color SUMMARY_ENGINE_COLOR = new Color(0.9f, 0.9f, 1f, 1f);
  private static final Color SUMMARY_INFO_COLOR = new Color(0.7f, 0.8f, 0.9f, 1f);
  private static final Color HEADER_COLOR = new Color(0.5f, 0.5f, 0.6f, 1f);

  private final ProfiledEngine engine;
  private final ObjectMap<Class<? extends EntitySystem>, Float> smoothedTimes = new ObjectMap<>();
  private final Array<SystemMetric> sortedMetrics = new Array<>();

  private boolean active;
  private float updateTimer;
  private float smoothedTotalTime;

  private Label totalTimeLabel;
  private Label entityCountLabel;
  private Label systemCountLabel;
  private Table systemsTable;
  private Skin panelSkin;

  public EcsMetricsPanel(ProfiledEngine engine) {
    this.engine = Objects.requireNonNull(engine, "engine");
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public void update(float delta) {
    updateTimer += delta;
    if (updateTimer < UPDATE_INTERVAL) {
      return;
    }

    updateTimer = ZERO_TIME_MS;
    updateSmoothedValues();
    updateSummaryLabels();
    updateSystemsTable();
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
  public Actor buildPanel(Skin skin) {
    panelSkin = skin;

    Table mainTable = new Table(skin);
    mainTable.top().left();
    mainTable.pad(MAIN_TABLE_PADDING);

    addSummarySection(mainTable, skin);
    addResetButton(mainTable, skin);
    addSystemsSection(mainTable, skin);

    return mainTable;
  }

  static String formatSystemName(String systemClassName) {
    String formattedName = systemClassName;
    if (formattedName.endsWith(SYSTEM_SUFFIX)) {
      formattedName = formattedName.substring(0, formattedName.length() - SYSTEM_SUFFIX.length());
    }
    if (formattedName.length() > MAX_VISIBLE_NAME_LENGTH) {
      return formattedName.substring(0, TRUNCATED_NAME_LENGTH) + NAME_ELLIPSIS;
    }
    return formattedName;
  }

  static Color getColorForTime(float timeMs) {
    if (timeMs < GOOD_TIME_THRESHOLD_MS) {
      return Color.GREEN;
    }
    if (timeMs < WARNING_TIME_THRESHOLD_MS) {
      return Color.YELLOW;
    }
    return Color.RED;
  }

  private void updateSmoothedValues() {
    smoothedTotalTime = interpolate(smoothedTotalTime, engine.getTotalUpdateTimeMs());

    ObjectMap<Class<? extends EntitySystem>, Long> times = engine.getSystemTimes();
    for (ObjectMap.Entry<Class<? extends EntitySystem>, Long> entry : times) {
      float currentTimeMs = nanosToMilliseconds(entry.value);
      Float previousTimeMs = smoothedTimes.get(entry.key);
      float smoothedTimeMs =
          previousTimeMs != null ? interpolate(previousTimeMs, currentTimeMs) : currentTimeMs;
      smoothedTimes.put(entry.key, smoothedTimeMs);
    }
  }

  private float interpolate(float previousValue, float currentValue) {
    return previousValue + (currentValue - previousValue) * SMOOTHING_FACTOR;
  }

  private void updateSummaryLabels() {
    totalTimeLabel.setText(String.format(Locale.ROOT, ENGINE_TIME_TEMPLATE, smoothedTotalTime));
    entityCountLabel.setText(
        String.format(Locale.ROOT, ENTITIES_TEMPLATE, engine.getEntities().size()));
    systemCountLabel.setText(
        String.format(Locale.ROOT, SYSTEMS_TEMPLATE, engine.getSystems().size()));
  }

  private void updateSystemsTable() {
    systemsTable.clearChildren();
    addTableHeader();
    buildSortedMetrics();
    addSystemRows();
  }

  private void addTableHeader() {
    Label nameHeader = new Label(SYSTEM_HEADER, panelSkin);
    Label timeHeader = new Label(TIME_HEADER, panelSkin);
    Label barHeader = new Label(LOAD_HEADER, panelSkin);

    nameHeader.setColor(HEADER_COLOR);
    timeHeader.setColor(HEADER_COLOR);
    barHeader.setColor(HEADER_COLOR);

    systemsTable.add(nameHeader).width(NAME_COLUMN_WIDTH).left();
    systemsTable.add(timeHeader).width(TIME_COLUMN_WIDTH).right();
    systemsTable.add(barHeader).width(BAR_WIDTH).left();
    systemsTable.row();
  }

  private void buildSortedMetrics() {
    sortedMetrics.clear();

    for (ObjectMap.Entry<Class<? extends EntitySystem>, Float> entry : smoothedTimes) {
      sortedMetrics.add(new SystemMetric(entry.key, entry.value));
    }

    sortedMetrics.sort((first, second) -> Float.compare(second.timeMs, first.timeMs));
  }

  private void addSystemRows() {
    int displayCount = Math.min(sortedMetrics.size, MAX_SYSTEMS_DISPLAY);
    for (int index = 0; index < displayCount; index++) {
      addSystemRow(sortedMetrics.get(index));
    }
  }

  private void addSystemRow(SystemMetric metric) {
    float loadRatio = calculateLoadRatio(metric.timeMs);
    Color rowColor = getColorForTime(metric.timeMs);

    Label nameLabel = new Label(formatSystemName(metric.systemClass.getSimpleName()), panelSkin);
    Label timeLabel =
        new Label(String.format(Locale.ROOT, SYSTEM_TIME_TEMPLATE, metric.timeMs), panelSkin);
    ProgressBar loadBar = createLoadBar(loadRatio, rowColor);

    nameLabel.setColor(rowColor);
    timeLabel.setColor(rowColor);

    systemsTable.add(nameLabel).width(NAME_COLUMN_WIDTH).left();
    systemsTable.add(timeLabel).width(TIME_COLUMN_WIDTH).right();
    systemsTable.add(loadBar).width(BAR_WIDTH).height(BAR_HEIGHT).left();
    systemsTable.row();
  }

  private float calculateLoadRatio(float systemTimeMs) {
    if (smoothedTotalTime <= ZERO_TIME_MS) {
      return ZERO_TIME_MS;
    }
    return Math.min(systemTimeMs / smoothedTotalTime, PROGRESS_BAR_MAX);
  }

  private ProgressBar createLoadBar(float loadRatio, Color rowColor) {
    ProgressBar progressBar =
        new ProgressBar(PROGRESS_BAR_MIN, PROGRESS_BAR_MAX, PROGRESS_BAR_STEP, false, panelSkin);
    progressBar.setValue(loadRatio);
    progressBar.setColor(rowColor);
    return progressBar;
  }

  private void addSummarySection(Table mainTable, Skin skin) {
    totalTimeLabel = new Label(ENGINE_PLACEHOLDER, skin);
    entityCountLabel = new Label(ENTITIES_PLACEHOLDER, skin);
    systemCountLabel = new Label(SYSTEMS_PLACEHOLDER, skin);

    totalTimeLabel.setColor(SUMMARY_ENGINE_COLOR);
    entityCountLabel.setColor(SUMMARY_INFO_COLOR);
    systemCountLabel.setColor(SUMMARY_INFO_COLOR);

    mainTable.add(totalTimeLabel).left().colspan(3).row();
    mainTable.add(entityCountLabel).left().colspan(3).row();
    mainTable.add(systemCountLabel).left().colspan(3).row();
    mainTable.add().height(SECTION_SPACING).row();
  }

  private void addResetButton(Table mainTable, Skin skin) {
    TextButton resetButton = new TextButton(RESET_PEAKS_LABEL, skin);
    resetButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            engine.resetPeakTimes();
            smoothedTimes.clear();
          }
        });
    mainTable.add(resetButton).left().colspan(3).row();
    mainTable.add().height(SECTION_SPACING).row();
  }

  private void addSystemsSection(Table mainTable, Skin skin) {
    systemsTable = new Table(skin);
    systemsTable.top().left();

    ScrollPane scrollPane = new ScrollPane(systemsTable, skin);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollingDisabled(true, false);

    mainTable.add(scrollPane).expand().fill().colspan(3).row();
  }

  private float nanosToMilliseconds(long nanoseconds) {
    return nanoseconds / 1_000_000f;
  }

  private static final class SystemMetric {
    private final Class<? extends EntitySystem> systemClass;
    private final float timeMs;

    private SystemMetric(Class<? extends EntitySystem> systemClass, float timeMs) {
      this.systemClass = systemClass;
      this.timeMs = timeMs;
    }
  }
}
