package io.github.proyectoM.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.proyectoM.Main;
import io.github.proyectoM.audio.AudioManager;
import io.github.proyectoM.registry.BulletRegistry;
import io.github.proyectoM.registry.CompanionRegistry;
import io.github.proyectoM.registry.EnemyRegistry;
import io.github.proyectoM.registry.MapRegistry;
import io.github.proyectoM.registry.VisualAssetRegistry;
import io.github.proyectoM.registry.WeaponRegistry;
import io.github.proyectoM.resources.Assets;
import io.github.proyectoM.templates.WeaponTemplate;

/** Displays loading progress while maps, skins, audio, and visual assets are initialized. */
public class LoadingScreen implements Screen {
  private static final String[] MAPS = {"maps/newIsometricMap.tmx"};
  private static final String[] UI_SKINS = {"skins/pruebaInterfaz/pruebaInterfaz.json"};

  private final LoadingScreenRenderer renderer;

  private float progress = 0f;
  private boolean finished = false;

  /** Creates the loading screen and queues the assets needed before the main menu opens. */
  public LoadingScreen(Main game) {
    ScreenViewport viewport = new ScreenViewport(new OrthographicCamera());
    this.renderer = new LoadingScreenRenderer(viewport, game.getBatch(), game.getShapeRenderer());

    loadEntityRegistries();
    loadMaps();
    loadUiSkins();
    loadAudioAssets();
    initializeAtlases();
  }

  private void loadEntityRegistries() {
    WeaponRegistry.getInstance().load();
    BulletRegistry.getInstance().load();
    EnemyRegistry.getInstance().load();
    CompanionRegistry.getInstance().load();
    MapRegistry.getInstance().load();
  }

  private void loadMaps() {
    for (String map : MAPS) {
      Assets.getManager().load(map, TiledMap.class);
    }
  }

  private void loadUiSkins() {
    for (String skin : UI_SKINS) {
      Assets.getManager().load(skin, Skin.class);
    }
  }

  private void loadAudioAssets() {
    AudioManager audioManager = AudioManager.getInstance();
    for (WeaponTemplate weapon : WeaponRegistry.getInstance().getAll().values()) {
      if (weapon.sound != null && !weapon.sound.isEmpty()) {
        String soundPath = "audio/sfx/weapons/" + weapon.sound + ".wav";
        audioManager.loadSound(weapon.sound, soundPath);
      }
    }
  }

  private void initializeAtlases() {
    VisualAssetRegistry.collectAndRequestAtlases(Assets.getManager());
    loadBulletSprites();
  }

  private void loadBulletSprites() {
    for (String spritePath : BulletRegistry.getInstance().getAllSpritePaths()) {
      Assets.getManager().load(spritePath, Texture.class);
    }
  }

  private void finalizeAnimations() {
    VisualAssetRegistry.loadAllAnimations(Assets.getManager());
  }

  @Override
  public void show() {
    renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  @Override
  public void render(float delta) {
    if (!finished) {
      if (Assets.getManager().update()) {
        finalizeAnimations();
        finished = true;
        ScreenManager.getInstance().showScreen(ScreenManager.ScreenType.MAIN_MENU);
        return;
      }
      progress = Assets.getManager().getProgress();
    }

    renderer.render(delta, progress);
  }

  @Override
  public void resize(int width, int height) {
    renderer.resize(width, height);
  }

  /** Not used in the loading screen. */
  @Override
  public void pause() {}

  /** Not used in the loading screen. */
  @Override
  public void resume() {}

  /** Not used in the loading screen. */
  @Override
  public void hide() {}

  @Override
  public void dispose() {
    renderer.dispose();
  }
}
