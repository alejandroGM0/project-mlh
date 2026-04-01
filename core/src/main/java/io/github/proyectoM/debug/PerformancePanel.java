package io.github.proyectoM.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.Locale;

/** Debug panel that displays frame and memory metrics. */
public final class PerformancePanel implements DebugPanel {
  private static final String TITLE = "Performance";
  private static final String FPS_TEMPLATE = "FPS: %d";
  private static final String DELTA_TEMPLATE = "Delta: %.4f";
  private static final String MEMORY_TEMPLATE = "Mem: %d / %d MB";
  private static final String FPS_PLACEHOLDER = "FPS: ...";
  private static final String DELTA_PLACEHOLDER = "Delta: ...";
  private static final String MEMORY_PLACEHOLDER = "Mem: ...";
  private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

  private static final int GOOD_FPS_THRESHOLD = 55;
  private static final int WARNING_FPS_THRESHOLD = 30;
  private static final int LABEL_PAD = 4;
  private static final Color GOOD_COLOR = new Color(0.3f, 1f, 0.3f, 1f);
  private static final Color WARNING_COLOR = new Color(1f, 1f, 0.3f, 1f);
  private static final Color CRITICAL_COLOR = new Color(1f, 0.3f, 0.3f, 1f);
  private static final Color INFO_COLOR = new Color(0.7f, 0.8f, 0.9f, 1f);

  private boolean active;
  private Label fpsLabel;
  private Label deltaLabel;
  private Label memLabel;

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public void update(float delta) {
    if (fpsLabel != null) {
      int fps = Gdx.graphics.getFramesPerSecond();
      fpsLabel.setText(formatFps(fps));
      fpsLabel.setColor(getFpsColor(fps));
    }
    if (deltaLabel != null) {
      deltaLabel.setText(formatDeltaSeconds(Gdx.graphics.getDeltaTime()));
    }
    if (memLabel != null) {
      memLabel.setText(
          formatMemoryUsageMegabytes(getUsedMemoryMegabytes(), getMaxMemoryMegabytes()));
    }
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
    Table table = new Table(skin);
    table.top().left();
    table.pad(LABEL_PAD);

    fpsLabel = new Label(FPS_PLACEHOLDER, skin);
    fpsLabel.setColor(GOOD_COLOR);
    deltaLabel = new Label(DELTA_PLACEHOLDER, skin);
    deltaLabel.setColor(INFO_COLOR);
    memLabel = new Label(MEMORY_PLACEHOLDER, skin);
    memLabel.setColor(INFO_COLOR);

    table.add(fpsLabel).left().padBottom(LABEL_PAD).row();
    table.add(deltaLabel).left().padBottom(LABEL_PAD).row();
    table.add(memLabel).left().row();
    return table;
  }

  /**
   * Returns a color reflecting the current framerate health.
   *
   * @param fps current frames per second
   * @return green for good, yellow for warning, red for critical
   */
  static Color getFpsColor(int fps) {
    if (fps >= GOOD_FPS_THRESHOLD) {
      return GOOD_COLOR;
    }
    if (fps >= WARNING_FPS_THRESHOLD) {
      return WARNING_COLOR;
    }
    return CRITICAL_COLOR;
  }

  static String formatFps(int framesPerSecond) {
    return String.format(Locale.ROOT, FPS_TEMPLATE, framesPerSecond);
  }

  static String formatDeltaSeconds(float deltaSeconds) {
    return String.format(Locale.ROOT, DELTA_TEMPLATE, deltaSeconds);
  }

  static String formatMemoryUsageMegabytes(long usedMegabytes, long maxMegabytes) {
    return String.format(Locale.ROOT, MEMORY_TEMPLATE, usedMegabytes, maxMegabytes);
  }

  private long getUsedMemoryMegabytes() {
    long totalMemoryBytes = Runtime.getRuntime().totalMemory();
    long freeMemoryBytes = Runtime.getRuntime().freeMemory();
    return (totalMemoryBytes - freeMemoryBytes) / BYTES_PER_MEGABYTE;
  }

  private long getMaxMemoryMegabytes() {
    return Runtime.getRuntime().maxMemory() / BYTES_PER_MEGABYTE;
  }
}
