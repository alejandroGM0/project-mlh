package io.github.proyectoM.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

/** Manages persistent user settings through LibGDX preferences. */
public class GameSettings {
  private static final String PREFERENCES_NAME = "proyectom-settings";

  private static final String KEY_MASTER_VOLUME = "master_volume";
  private static final String KEY_SFX_VOLUME = "sfx_volume";
  private static final String KEY_MUSIC_VOLUME = "music_volume";
  private static final String KEY_AMBIENT_VOLUME = "ambient_volume";
  private static final String KEY_SPATIAL_AUDIO = "spatial_audio_enabled";
  private static final String KEY_MAX_AUDIO_DISTANCE = "max_audio_distance";
  private static final String KEY_FULLSCREEN = "fullscreen";
  private static final String KEY_VSYNC = "vsync";
  private static final String KEY_RESOLUTION_WIDTH = "resolution_width";
  private static final String KEY_RESOLUTION_HEIGHT = "resolution_height";
  private static final String KEY_MOVE_UP = "key_move_up";
  private static final String KEY_MOVE_DOWN = "key_move_down";
  private static final String KEY_MOVE_LEFT = "key_move_left";
  private static final String KEY_MOVE_RIGHT = "key_move_right";
  private static final String KEY_SHOOT = "key_shoot";
  private static final String KEY_PAUSE = "key_pause";
  private static final String KEY_DEBUG = "key_debug";

  private static final float MIN_VOLUME = 0f;
  private static final float MAX_VOLUME = 1f;
  private static final float MIN_AUDIO_DISTANCE = 0f;
  private static final float DEFAULT_MASTER_VOLUME = 1f;
  private static final float DEFAULT_SFX_VOLUME = 0.8f;
  private static final float DEFAULT_MUSIC_VOLUME = 0.6f;
  private static final float DEFAULT_AMBIENT_VOLUME = 0.5f;
  private static final boolean DEFAULT_SPATIAL_AUDIO = true;
  private static final float DEFAULT_MAX_AUDIO_DISTANCE = 100f;
  private static final boolean DEFAULT_FULLSCREEN = false;
  private static final boolean DEFAULT_VSYNC = true;
  private static final int DEFAULT_RESOLUTION_WIDTH = 1900;
  private static final int DEFAULT_RESOLUTION_HEIGHT = 1200;
  private static final int DEFAULT_KEY_MOVE_UP = Input.Keys.W;
  private static final int DEFAULT_KEY_MOVE_DOWN = Input.Keys.S;
  private static final int DEFAULT_KEY_MOVE_LEFT = Input.Keys.A;
  private static final int DEFAULT_KEY_MOVE_RIGHT = Input.Keys.D;
  private static final int DEFAULT_KEY_SHOOT = Input.Buttons.LEFT;
  private static final int DEFAULT_KEY_PAUSE = Input.Keys.ESCAPE;
  private static final int DEFAULT_KEY_DEBUG = Input.Keys.F3;

  private static GameSettings instance;

  private final Preferences preferences;

  private GameSettings() {
    preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    loadDefaults();
  }

  public static synchronized GameSettings getInstance() {
    if (instance == null) {
      instance = new GameSettings();
    }
    return instance;
  }

  public float getMasterVolume() {
    return preferences.getFloat(KEY_MASTER_VOLUME, DEFAULT_MASTER_VOLUME);
  }

  public void setMasterVolume(float volume) {
    preferences.putFloat(KEY_MASTER_VOLUME, clampVolume(volume));
  }

  public float getSfxVolume() {
    return preferences.getFloat(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME);
  }

  public void setSfxVolume(float volume) {
    preferences.putFloat(KEY_SFX_VOLUME, clampVolume(volume));
  }

  public float getMusicVolume() {
    return preferences.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
  }

  public void setMusicVolume(float volume) {
    preferences.putFloat(KEY_MUSIC_VOLUME, clampVolume(volume));
  }

  public float getAmbientVolume() {
    return preferences.getFloat(KEY_AMBIENT_VOLUME, DEFAULT_AMBIENT_VOLUME);
  }

  public void setAmbientVolume(float volume) {
    preferences.putFloat(KEY_AMBIENT_VOLUME, clampVolume(volume));
  }

  public boolean isSpatialAudio() {
    return preferences.getBoolean(KEY_SPATIAL_AUDIO, DEFAULT_SPATIAL_AUDIO);
  }

  public void setSpatialAudio(boolean enabled) {
    preferences.putBoolean(KEY_SPATIAL_AUDIO, enabled);
  }

  public float getMaxAudioDistance() {
    return preferences.getFloat(KEY_MAX_AUDIO_DISTANCE, DEFAULT_MAX_AUDIO_DISTANCE);
  }

  public void setMaxAudioDistance(float distance) {
    preferences.putFloat(KEY_MAX_AUDIO_DISTANCE, Math.max(MIN_AUDIO_DISTANCE, distance));
  }

  public boolean isFullscreen() {
    return preferences.getBoolean(KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
  }

  public void setFullscreen(boolean fullscreen) {
    preferences.putBoolean(KEY_FULLSCREEN, fullscreen);
  }

  public boolean isVsync() {
    return preferences.getBoolean(KEY_VSYNC, DEFAULT_VSYNC);
  }

  public void setVsync(boolean vsync) {
    preferences.putBoolean(KEY_VSYNC, vsync);
  }

  public int getResolutionWidth() {
    return preferences.getInteger(KEY_RESOLUTION_WIDTH, DEFAULT_RESOLUTION_WIDTH);
  }

  public int getResolutionHeight() {
    return preferences.getInteger(KEY_RESOLUTION_HEIGHT, DEFAULT_RESOLUTION_HEIGHT);
  }

  public void setResolution(int width, int height) {
    preferences.putInteger(KEY_RESOLUTION_WIDTH, width);
    preferences.putInteger(KEY_RESOLUTION_HEIGHT, height);
  }

  public int getMoveUpKey() {
    return preferences.getInteger(KEY_MOVE_UP, DEFAULT_KEY_MOVE_UP);
  }

  public void setMoveUpKey(int keyCode) {
    preferences.putInteger(KEY_MOVE_UP, keyCode);
  }

  public int getMoveDownKey() {
    return preferences.getInteger(KEY_MOVE_DOWN, DEFAULT_KEY_MOVE_DOWN);
  }

  public void setMoveDownKey(int keyCode) {
    preferences.putInteger(KEY_MOVE_DOWN, keyCode);
  }

  public int getMoveLeftKey() {
    return preferences.getInteger(KEY_MOVE_LEFT, DEFAULT_KEY_MOVE_LEFT);
  }

  public void setMoveLeftKey(int keyCode) {
    preferences.putInteger(KEY_MOVE_LEFT, keyCode);
  }

  public int getMoveRightKey() {
    return preferences.getInteger(KEY_MOVE_RIGHT, DEFAULT_KEY_MOVE_RIGHT);
  }

  public void setMoveRightKey(int keyCode) {
    preferences.putInteger(KEY_MOVE_RIGHT, keyCode);
  }

  public int getShootKey() {
    return preferences.getInteger(KEY_SHOOT, DEFAULT_KEY_SHOOT);
  }

  public void setShootKey(int keyCode) {
    preferences.putInteger(KEY_SHOOT, keyCode);
  }

  public int getPauseKey() {
    return preferences.getInteger(KEY_PAUSE, DEFAULT_KEY_PAUSE);
  }

  public void setPauseKey(int keyCode) {
    preferences.putInteger(KEY_PAUSE, keyCode);
  }

  public int getDebugKey() {
    return preferences.getInteger(KEY_DEBUG, DEFAULT_KEY_DEBUG);
  }

  public void setDebugKey(int keyCode) {
    preferences.putInteger(KEY_DEBUG, keyCode);
  }

  public void save() {
    preferences.flush();
  }

  public void resetToDefaults() {
    preferences.clear();
    loadDefaults();
  }

  public boolean contains(String key) {
    return preferences.contains(key);
  }

  public static void reset() {
    instance = null;
  }

  private void loadDefaults() {
    if (preferences.contains(KEY_MASTER_VOLUME)) {
      return;
    }

    setMasterVolume(DEFAULT_MASTER_VOLUME);
    setSfxVolume(DEFAULT_SFX_VOLUME);
    setMusicVolume(DEFAULT_MUSIC_VOLUME);
    setAmbientVolume(DEFAULT_AMBIENT_VOLUME);
    setSpatialAudio(DEFAULT_SPATIAL_AUDIO);
    setMaxAudioDistance(DEFAULT_MAX_AUDIO_DISTANCE);
    setFullscreen(DEFAULT_FULLSCREEN);
    setVsync(DEFAULT_VSYNC);
    setResolution(DEFAULT_RESOLUTION_WIDTH, DEFAULT_RESOLUTION_HEIGHT);
    setMoveUpKey(DEFAULT_KEY_MOVE_UP);
    setMoveDownKey(DEFAULT_KEY_MOVE_DOWN);
    setMoveLeftKey(DEFAULT_KEY_MOVE_LEFT);
    setMoveRightKey(DEFAULT_KEY_MOVE_RIGHT);
    setShootKey(DEFAULT_KEY_SHOOT);
    setPauseKey(DEFAULT_KEY_PAUSE);
    setDebugKey(DEFAULT_KEY_DEBUG);
    save();
  }

  private float clampVolume(float volume) {
    return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, volume));
  }
}
