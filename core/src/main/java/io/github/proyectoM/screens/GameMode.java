package io.github.proyectoM.screens;

import io.github.proyectoM.localization.LocalizationManager;

/** Represents the game modes shown in the setup screen. */
public enum GameMode {
  SURVIVAL("gameSetup.mode.survival", "gameSetup.mode.survival.desc", true),
  ENDLESS("gameSetup.mode.endless", "gameSetup.mode.endless.desc", false),
  TIME_ATTACK("gameSetup.mode.timeAttack", "gameSetup.mode.timeAttack.desc", false);

  private final String nameKey;
  private final String descriptionKey;
  private final boolean unlocked;

  GameMode(String nameKey, String descriptionKey, boolean unlocked) {
    this.nameKey = nameKey;
    this.descriptionKey = descriptionKey;
    this.unlocked = unlocked;
  }

  public String getDisplayName() {
    return LocalizationManager.getInstance().get(nameKey);
  }

  public String getDescription() {
    return LocalizationManager.getInstance().get(descriptionKey);
  }

  public boolean isUnlocked() {
    return unlocked;
  }

  public String getNameKey() {
    return nameKey;
  }
}
