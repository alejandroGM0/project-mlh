package io.github.proyectoM.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Centralized manager for in-game debug panels and their windows. */
public class DebugSystem {
  private static final int INITIAL_WINDOW_WIDTH = 420;
  private static final int INITIAL_WINDOW_HEIGHT = 350;
  private static final int PERFORMANCE_PANEL_WIDTH = 300;
  private static final int PERFORMANCE_PANEL_HEIGHT = 160;
  private static final int ENTITY_SPAWNER_PANEL_WIDTH = 440;
  private static final int ENTITY_SPAWNER_PANEL_HEIGHT = 500;
  private static final int CONTROL_PANEL_WIDTH = 300;
  private static final int CONTROL_PANEL_HEIGHT = 450;
  private static final int WINDOW_MARGIN = 20;
  private static final int WINDOW_PADDING = 10;
  private static final int CATEGORY_SPACER_HEIGHT = 6;
  private static final int CONTROL_ENTRY_PADDING = 5;
  private static final int CLOSE_BUTTON_SIZE = 20;
  private static final int CLOSE_BUTTON_RIGHT_PADDING = 2;

  private static final String SKIN_PATH = "skins/default/uiskin.json";
  private static final String CONTROL_PANEL_TITLE = "DEBUG MODE";
  private static final String PANELS_LABEL = "Panels  [F1 toggle | F2 menu]";
  private static final String BUTTON_ON = " [ON]";
  private static final String BUTTON_OFF = " [OFF]";
  private static final String CLOSE_BUTTON_TEXT = "X";
  private static final String DEBUG_SYSTEM_TAG = "DebugSystem";
  private static final String PANEL_RENDER_ERROR_TEMPLATE = "Error rendering panel ";

  private static final String DEBUG_WINDOW_STYLE_NAME = "debug";
  private static final Color DEBUG_WINDOW_BG_COLOR = new Color(0.06f, 0.08f, 0.1f, 0.92f);
  private static final Color DEBUG_TITLE_FONT_COLOR = new Color(0.4f, 1f, 0.4f, 1f);
  private static final Color TOGGLE_ON_COLOR = new Color(0.3f, 1f, 0.3f, 1f);
  private static final Color TOGGLE_OFF_COLOR = new Color(0.6f, 0.6f, 0.6f, 1f);
  private static final Color CLOSE_BUTTON_TEXT_COLOR = new Color(1f, 0.4f, 0.4f, 1f);
  private static final Color CATEGORY_COLOR = new Color(0.3f, 0.9f, 1f, 1f);
  private static final Color PANELS_LABEL_COLOR = new Color(0.6f, 0.8f, 0.6f, 1f);
  private static final int DEBUG_WINDOW_PAD_LEFT = 6;
  private static final int DEBUG_WINDOW_PAD_RIGHT = 6;
  private static final int DEBUG_WINDOW_PAD_TOP = 22;
  private static final int DEBUG_WINDOW_PAD_BOTTOM = 4;

  private static DebugSystem instance;

  private final Stage stage;
  private final ShapeRenderer shapeRenderer;
  private final Skin skin;
  private final DebugWindowLayout windowLayout;
  private final List<DebugPanelRegistration> panelRegistrations = new ArrayList<>();

  private boolean visible;
  private InputMultiplexer multiplexer;
  private InputProcessor previousInput;
  private Window controlPanel;
  private Table controlTable;
  private Texture debugWindowBgTexture;

  private DebugSystem() {
    stage = new Stage(new ScreenViewport());
    skin = new Skin(Gdx.files.internal(SKIN_PATH));
    shapeRenderer = new ShapeRenderer();
    windowLayout = new DebugWindowLayout(WINDOW_MARGIN, WINDOW_PADDING);
    buildDebugStyles();
    createControlPanel();
  }

  public static DebugSystem getInstance() {
    if (instance == null) {
      instance = new DebugSystem();
    }
    return instance;
  }

  /**
   * Creates semi-transparent window backgrounds and themed styles for the debug overlay using
   * programmatic Pixmap-based drawables.
   */
  private void buildDebugStyles() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(DEBUG_WINDOW_BG_COLOR);
    pixmap.fill();
    debugWindowBgTexture = new Texture(pixmap);
    pixmap.dispose();

    TextureRegionDrawable bgDrawable =
        new TextureRegionDrawable(new TextureRegion(debugWindowBgTexture));
    bgDrawable.setLeftWidth(DEBUG_WINDOW_PAD_LEFT);
    bgDrawable.setRightWidth(DEBUG_WINDOW_PAD_RIGHT);
    bgDrawable.setTopHeight(DEBUG_WINDOW_PAD_TOP);
    bgDrawable.setBottomHeight(DEBUG_WINDOW_PAD_BOTTOM);

    Window.WindowStyle debugStyle = new Window.WindowStyle();
    debugStyle.background = bgDrawable;
    debugStyle.titleFont = skin.getFont("default-font");
    debugStyle.titleFontColor = DEBUG_TITLE_FONT_COLOR;
    skin.add(DEBUG_WINDOW_STYLE_NAME, debugStyle);
  }

  /** Registers a panel without category metadata. */
  public void addDebugPanel(DebugPanel panel) {
    addDebugPanel(panel, null);
  }

  /** Registers a panel and creates its floating window if the title is not already present. */
  public void addDebugPanel(DebugPanel panel, String category) {
    DebugPanel requiredPanel = Objects.requireNonNull(panel, "panel");
    if (hasPanelWithTitle(requiredPanel.getTitle())) {
      return;
    }

    panelRegistrations.add(createRegistration(requiredPanel, category));
    updateControlPanel();
  }

  /** Removes every registered panel window but keeps the debug system itself alive. */
  public void clearPanels() {
    for (DebugPanelRegistration registration : panelRegistrations) {
      registration.getWindow().remove();
    }
    panelRegistrations.clear();
    updateControlPanel();
  }

  /** Handles debug hotkeys, updates active panels, and advances the Scene2D stage. */
  public void update(float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
      setVisible(!visible);
    }

    if (visible && Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
      controlPanel.setVisible(!controlPanel.isVisible());
    }

    if (!visible) {
      return;
    }

    for (DebugPanelRegistration registration : panelRegistrations) {
      if (registration.isActive()) {
        registration.getPanel().update(delta);
      }
    }

    stage.act(delta);
  }

  /** Draws the control stage and any active overlay renderers owned by debug panels. */
  public void render() {
    if (!visible) {
      return;
    }

    stage.draw();
    shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    for (DebugPanelRegistration registration : panelRegistrations) {
      if (registration.isActive()) {
        renderPanel(registration.getPanel());
      }
    }
    shapeRenderer.end();
  }

  /** Resizes the shared debug stage viewport to match the game window. */
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  /** Releases all UI/render resources and clears the singleton instance. */
  public void dispose() {
    clearPanels();
    stage.dispose();
    skin.dispose();
    shapeRenderer.dispose();
    debugWindowBgTexture.dispose();
    instance = null;
  }

  public void show() {
    setVisible(true);
  }

  public void hide() {
    setVisible(false);
  }

  /**
   * Shows or hides the whole debug UI.
   *
   * <p>When showing, windows are repacked to avoid overlap and the debug stage is inserted into the
   * current input chain.
   */
  public void setVisible(boolean show) {
    visible = show;
    if (show) {
      repositionAllWindows();
    }

    updateWindowVisibility(show);
    controlPanel.setVisible(show);
    updateInputProcessors(show);
  }

  public Stage getStage() {
    return stage;
  }

  public boolean isVisible() {
    return visible;
  }

  private void createControlPanel() {
    controlPanel = new Window(CONTROL_PANEL_TITLE, skin, DEBUG_WINDOW_STYLE_NAME);
    controlPanel.setMovable(true);
    controlPanel.setPosition(
        Gdx.graphics.getWidth() - CONTROL_PANEL_WIDTH - WINDOW_MARGIN,
        Gdx.graphics.getHeight() - CONTROL_PANEL_HEIGHT - WINDOW_MARGIN);
    controlPanel.setSize(CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);

    controlTable = new Table(skin);
    controlTable.top().left();
    controlTable.defaults().pad(CONTROL_ENTRY_PADDING).expandX().fillX();
    addPanelsLabel();

    ScrollPane controlScrollPane = new ScrollPane(controlTable, skin);
    controlScrollPane.setScrollingDisabled(true, false);
    controlScrollPane.setFadeScrollBars(false);

    controlPanel.add(controlScrollPane).expand().fill();
    controlPanel.setVisible(false);
    stage.addActor(controlPanel);
  }

  private void addPanelsLabel() {
    Label panelsLabel = new Label(PANELS_LABEL, skin);
    panelsLabel.setColor(PANELS_LABEL_COLOR);
    controlTable.add(panelsLabel).left().row();
  }

  private boolean hasPanelWithTitle(String title) {
    for (DebugPanelRegistration registration : panelRegistrations) {
      if (registration.hasTitle(title)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Bundles the panel, its window, and the optional category together so the manager does not have
   * to keep parallel collections in sync.
   */
  private DebugPanelRegistration createRegistration(DebugPanel panel, String category) {
    DebugWindowBounds panelBounds = getPanelBounds(panel);
    DebugWindowBounds positionedBounds =
        findAvailableBounds(panelBounds.getWidth(), panelBounds.getHeight());

    Window window = createPanelWindow(panel, positionedBounds);
    DebugPanelRegistration registration = new DebugPanelRegistration(panel, window, category);
    configureWindowTitleBar(registration);
    window.add(panel.buildPanel(skin)).expand().fill();
    window.setVisible(visible && panel.isActive());
    stage.addActor(window);
    return registration;
  }

  private Window createPanelWindow(DebugPanel panel, DebugWindowBounds bounds) {
    Window window = new Window(panel.getTitle(), skin, DEBUG_WINDOW_STYLE_NAME);
    window.setMovable(true);
    window.setResizable(true);
    window.setSize(bounds.getWidth(), bounds.getHeight());
    window.setPosition(bounds.getX(), bounds.getY());
    return window;
  }

  private DebugWindowBounds getPanelBounds(DebugPanel panel) {
    if (panel instanceof PerformancePanel) {
      return DebugWindowBounds.of(0, 0, PERFORMANCE_PANEL_WIDTH, PERFORMANCE_PANEL_HEIGHT);
    }
    if (panel instanceof EntitySpawnerPanel) {
      return DebugWindowBounds.of(0, 0, ENTITY_SPAWNER_PANEL_WIDTH, ENTITY_SPAWNER_PANEL_HEIGHT);
    }
    return DebugWindowBounds.of(0, 0, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
  }

  private void updateControlPanel() {
    controlTable.clear();
    addPanelsLabel();

    String lastCategory = null;
    for (DebugPanelRegistration registration : panelRegistrations) {
      String category = registration.getCategory();
      if (category != null && !category.equals(lastCategory)) {
        controlTable.add().height(CATEGORY_SPACER_HEIGHT).row();
        Label categoryLabel = new Label(category, skin);
        categoryLabel.setColor(CATEGORY_COLOR);
        controlTable.add(categoryLabel).left().row();
        lastCategory = category;
      }

      controlTable.add(createToggleButton(registration)).row();
    }
  }

  private TextButton createToggleButton(DebugPanelRegistration registration) {
    TextButton toggleButton = new TextButton(buildToggleLabel(registration), skin);
    applyToggleButtonColor(toggleButton, registration.isActive());
    toggleButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            toggleRegistration(registration, toggleButton);
          }
        });
    return toggleButton;
  }

  private String buildToggleLabel(DebugPanelRegistration registration) {
    return registration.getTitle() + (registration.isActive() ? BUTTON_ON : BUTTON_OFF);
  }

  /** Keeps the panel state and the actual window visibility synchronized from one place. */
  private void toggleRegistration(DebugPanelRegistration registration, TextButton toggleButton) {
    boolean newState = !registration.isActive();
    registration.getPanel().setActive(newState);
    toggleButton.setText(buildToggleLabel(registration));
    applyToggleButtonColor(toggleButton, newState);
    registration.getWindow().setVisible(newState);
  }

  /**
   * Applies green or gray coloring to a toggle button label based on active state.
   *
   * @param button the toggle button to style
   * @param active whether the associated panel is active
   */
  private void applyToggleButtonColor(TextButton button, boolean active) {
    button.getLabel().setColor(active ? TOGGLE_ON_COLOR : TOGGLE_OFF_COLOR);
  }

  /**
   * Hidden windows do not reserve layout space, so reopening debug mode can repack only what the
   * user is actually looking at.
   */
  private DebugWindowBounds findAvailableBounds(int width, int height) {
    return windowLayout.findAvailableBounds(
        getVisibleWindowBounds(), width, height, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  private List<DebugWindowBounds> getVisibleWindowBounds() {
    List<DebugWindowBounds> bounds = new ArrayList<>();
    for (DebugPanelRegistration registration : panelRegistrations) {
      if (registration.getWindow().isVisible()) {
        bounds.add(DebugWindowBounds.fromWindow(registration.getWindow()));
      }
    }
    return bounds;
  }

  private void configureWindowTitleBar(DebugPanelRegistration registration) {
    Window window = registration.getWindow();
    window.getTitleTable().getCell(window.getTitleLabel()).expand().left();

    Button closeButton = new TextButton(CLOSE_BUTTON_TEXT, skin);
    ((TextButton) closeButton).getLabel().setColor(CLOSE_BUTTON_TEXT_COLOR);
    closeButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            handlePanelClose(registration);
          }
        });
    window
        .getTitleTable()
        .add(closeButton)
        .size(CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE)
        .padRight(CLOSE_BUTTON_RIGHT_PADDING);
  }

  private void renderPanel(DebugPanel panel) {
    try {
      panel.render(shapeRenderer);
    } catch (RuntimeException exception) {
      Gdx.app.error(DEBUG_SYSTEM_TAG, PANEL_RENDER_ERROR_TEMPLATE + panel.getTitle(), exception);
    }
  }

  private void updateWindowVisibility(boolean show) {
    for (DebugPanelRegistration registration : panelRegistrations) {
      if (registration.isActive()) {
        registration.getWindow().setVisible(show);
      }
    }
  }

  /**
   * The debug stage temporarily becomes the front input processor while preserving the previous one
   * so gameplay/UI input can be restored exactly when debug mode is hidden again.
   */
  private void updateInputProcessors(boolean show) {
    if (show) {
      previousInput = Gdx.input.getInputProcessor();
      multiplexer = new InputMultiplexer(stage);
      if (previousInput != null) {
        multiplexer.addProcessor(previousInput);
      }
      Gdx.input.setInputProcessor(multiplexer);
      return;
    }

    if (previousInput != null) {
      Gdx.input.setInputProcessor(previousInput);
    }
    previousInput = null;
  }

  /**
   * Rebuilds the window packing using only active panels so disabled panels never create dead gaps
   * in the visible debug layout.
   */
  private void repositionAllWindows() {
    List<DebugWindowBounds> occupiedBounds = new ArrayList<>();
    for (DebugPanelRegistration registration : panelRegistrations) {
      if (!registration.isActive()) {
        continue;
      }

      DebugWindowBounds panelBounds = getPanelBounds(registration.getPanel());
      DebugWindowBounds positionedBounds =
          windowLayout.findAvailableBounds(
              occupiedBounds,
              panelBounds.getWidth(),
              panelBounds.getHeight(),
              Gdx.graphics.getWidth(),
              Gdx.graphics.getHeight());
      registration.getWindow().setPosition(positionedBounds.getX(), positionedBounds.getY());
      occupiedBounds.add(positionedBounds);
    }
  }

  private void handlePanelClose(DebugPanelRegistration registration) {
    registration.getWindow().setVisible(false);
    registration.getPanel().setActive(false);
    updateControlPanel();
  }
}
