package io.github.proyectoM.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import io.github.proyectoM.components.sound.SoundCategory;
import io.github.proyectoM.settings.GameSettings;
import java.util.HashMap;
import java.util.Map;

/** Centralizes audio loading, caching, and playback. */
public class AudioManager implements Disposable {
  private static final float DEFAULT_VOLUME = 1f;
  private static final float SILENT_VOLUME = 0f;

  private static AudioManager instance;

  private final Map<String, Sound> soundCache = new HashMap<>();
  private final Map<String, Music> musicCache = new HashMap<>();
  private final GameSettings settings;

  private Music currentMusic;

  private AudioManager() {
    settings = GameSettings.getInstance();
  }

  public static AudioManager getInstance() {
    if (instance == null) {
      instance = new AudioManager();
    }
    return instance;
  }

  public void playSound(String soundName, float volume, SoundCategory category) {
    Sound sound = soundCache.get(soundName);
    if (sound == null) {
      return;
    }

    float finalVolume = calculateCategoryVolume(volume, category);
    if (finalVolume > SILENT_VOLUME) {
      sound.play(finalVolume);
    }
  }

  public void playSound(
      String soundName, float volume, float pitch, float pan, SoundCategory category) {
    Sound sound = soundCache.get(soundName);
    if (sound == null) {
      return;
    }

    float finalVolume = calculateCategoryVolume(volume, category);
    if (finalVolume > SILENT_VOLUME) {
      sound.play(finalVolume, pitch, pan);
    }
  }

  /**
   * Registers a sound file for later playback. Duplicate names are silently ignored.
   *
   * @param name logical identifier used when calling playSound
   * @param filePath internal asset path
   */
  public void loadSound(String name, String filePath) {
    if (soundCache.containsKey(name)) {
      return;
    }

    FileHandle soundFile = Gdx.files.internal(filePath);
    if (!soundFile.exists()) {
      return;
    }

    soundCache.put(name, Gdx.audio.newSound(soundFile));
  }

  public void playMusic(String musicName, boolean loop) {
    stopMusic();

    Music music = musicCache.get(musicName);
    if (music == null) {
      return;
    }

    currentMusic = music;
    currentMusic.setVolume(calculateMusicVolume());
    currentMusic.setLooping(loop);
    currentMusic.play();
  }

  public void stopMusic() {
    if (currentMusic != null && currentMusic.isPlaying()) {
      currentMusic.stop();
    }
    currentMusic = null;
  }

  public void pauseMusic() {
    if (currentMusic != null && currentMusic.isPlaying()) {
      currentMusic.pause();
    }
  }

  public void resumeMusic() {
    if (currentMusic != null) {
      currentMusic.play();
    }
  }

  public void updateVolumes() {
    if (currentMusic != null) {
      currentMusic.setVolume(calculateMusicVolume());
    }
  }

  @Override
  public void dispose() {
    stopMusic();
    disposeSounds();
    disposeMusic();
    instance = null;
  }

  private float calculateCategoryVolume(float baseVolume, SoundCategory category) {
    return baseVolume * resolveCategoryVolume(category) * settings.getMasterVolume();
  }

  private float resolveCategoryVolume(SoundCategory category) {
    switch (category) {
      case SFX:
      case VOICE:
      case UI:
        return settings.getSfxVolume();
      case MUSIC:
        return settings.getMusicVolume();
      case AMBIENT:
        return settings.getAmbientVolume();
      default:
        return settings.getSfxVolume();
    }
  }

  private float calculateMusicVolume() {
    return calculateCategoryVolume(DEFAULT_VOLUME, SoundCategory.MUSIC);
  }

  private void disposeSounds() {
    for (Sound sound : soundCache.values()) {
      sound.dispose();
    }
    soundCache.clear();
  }

  private void disposeMusic() {
    for (Music music : musicCache.values()) {
      music.dispose();
    }
    musicCache.clear();
  }
}
