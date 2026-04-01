package io.github.proyectoM.localization;

/** Type-safe access to localization bundle keys. */
public enum TranslationKeys {
  MENU_TITLE("menu.title"),
  MENU_START("menu.start"),
  MENU_UPGRADES("menu.upgrades"),
  MENU_OPTIONS("menu.options"),
  MENU_EXIT("menu.exit"),

  GAME_SETUP_TITLE("gameSetup.title"),
  GAME_SETUP_SELECT_MAP("gameSetup.selectMap"),
  GAME_SETUP_SELECT_MODE("gameSetup.selectMode"),
  GAME_SETUP_START("gameSetup.start"),
  GAME_SETUP_BACK("gameSetup.back"),
  GAME_SETUP_LOCKED("gameSetup.locked"),

  GAME_WAVE("game.wave"),
  GAME_ENEMIES("game.enemies"),
  GAME_HEALTH("game.health"),
  GAME_PAUSED("game.paused"),
  GAME_RESUME("game.resume"),
  GAME_MAIN_MENU("game.mainMenu"),
  GAME_OPTIONS("game.options"),
  GAME_EXIT("game.exit"),

  PLAYER_HEALTH("player.health"),
  PLAYER_DAMAGE("player.damage"),
  PLAYER_SPEED("player.speed"),
  PLAYER_LEVEL("player.level"),

  UPGRADES_TITLE("upgrades.title"),
  UPGRADES_PERMANENT("upgrades.permanent"),
  UPGRADES_WEAPONS("upgrades.weapons"),
  UPGRADES_COMPANIONS("upgrades.companions"),
  UPGRADES_POINTS("upgrades.points"),
  UPGRADES_COST("upgrades.cost"),
  UPGRADES_UNLOCK("upgrades.unlock"),
  UPGRADES_UPGRADE("upgrades.upgrade"),
  UPGRADES_MAX_LEVEL("upgrades.maxLevel"),
  UPGRADES_LOCKED("upgrades.locked"),
  UPGRADES_BACK("upgrades.back"),

  WEAPON_PISTOL("weapon.pistol"),
  WEAPON_RIFLE("weapon.rifle"),
  WEAPON_HEAVY_PISTOL("weapon.heavy_pistol"),
  WEAPON_SHOTGUN("weapon.shotgun"),
  WEAPON_SMG("weapon.smg"),

  COMPANION_SOLDIER("companion.soldier"),
  COMPANION_MEDIC("companion.medic"),
  COMPANION_SNIPER("companion.sniper"),

  ACHIEVEMENT_UNLOCKED("achievement.unlocked"),
  ACHIEVEMENT_FIRST_BLOOD("achievement.first_blood"),
  ACHIEVEMENT_SURVIVOR("achievement.survivor"),
  ACHIEVEMENT_WAVE_MASTER("achievement.wave_master"),

  UI_CONFIRM("ui.confirm"),
  UI_CANCEL("ui.cancel"),
  UI_YES("ui.yes"),
  UI_NO("ui.no"),
  UI_OK("ui.ok"),
  UI_SAVE("ui.save"),
  UI_LOAD("ui.load"),
  UI_DELETE("ui.delete"),
  UI_APPLY("ui.apply"),

  SETTINGS_TITLE("settings.title"),
  SETTINGS_LANGUAGE("settings.language"),
  SETTINGS_LANGUAGE_DESC("settings.language_desc"),
  SETTINGS_AUDIO("settings.audio"),
  SETTINGS_VIDEO("settings.video"),
  SETTINGS_GAMEPLAY("settings.gameplay"),
  SETTINGS_ACCESSIBILITY("settings.accessibility"),
  SETTINGS_SUBTITLE("settings.subtitle"),
  SETTINGS_CONTROLS("settings.controls"),
  SETTINGS_MASTER_VOLUME("settings.master_volume"),
  SETTINGS_MUSIC_VOLUME("settings.music_volume"),
  SETTINGS_SFX_VOLUME("settings.sfx_volume"),
  SETTINGS_FULLSCREEN("settings.fullscreen"),
  SETTINGS_VSYNC("settings.vsync"),
  SETTINGS_RESOLUTION("settings.resolution"),
  SETTINGS_RESOLUTION_DESC("settings.resolution_desc"),
  SETTINGS_QUALITY("settings.quality"),
  SETTINGS_QUALITY_DESC("settings.quality_desc"),
  SETTINGS_BRIGHTNESS("settings.brightness"),
  SETTINGS_BRIGHTNESS_DESC("settings.brightness_desc"),
  SETTINGS_MUTE_ALL("settings.mute_all"),
  SETTINGS_MUTE_ALL_DESC("settings.mute_all_desc"),
  SETTINGS_DIFFICULTY("settings.difficulty"),
  SETTINGS_DIFFICULTY_DESC("settings.difficulty_desc"),
  SETTINGS_SENSITIVITY("settings.sensitivity"),
  SETTINGS_SENSITIVITY_DESC("settings.sensitivity_desc"),
  SETTINGS_AUTOSAVE("settings.autosave"),
  SETTINGS_AUTOSAVE_DESC("settings.autosave_desc"),
  SETTINGS_SUBTITLES("settings.subtitles"),
  SETTINGS_SUBTITLES_DESC("settings.subtitles_desc"),
  SETTINGS_BACK("settings.back"),
  SETTINGS_APPLY("settings.apply"),
  SETTINGS_RESET("settings.reset"),
  SETTINGS_CONTROLS_PLACEHOLDER("settings.controls_placeholder"),
  SETTINGS_APPLIED_MSG("settings.applied"),
  SETTINGS_RESET_MSG("settings.reset_msg"),
  SETTINGS_READY("settings.ready"),
  SETTINGS_CONFIGURE_KEYS("settings.configure_keys"),
  SETTINGS_VIBRATION("settings.vibration"),
  SETTINGS_VIBRATION_DESC("settings.vibration_desc"),
  SETTINGS_COLORBLIND_MODE("settings.colorblind_mode"),
  SETTINGS_COLORBLIND_DESC("settings.colorblind_desc"),
  SETTINGS_TEXT_SIZE("settings.text_size"),
  SETTINGS_TEXT_SIZE_DESC("settings.text_size_desc"),
  SETTINGS_HIGH_CONTRAST("settings.high_contrast"),
  SETTINGS_HIGH_CONTRAST_DESC("settings.high_contrast_desc"),
  SETTINGS_REDUCE_MOTION("settings.reduce_motion"),
  SETTINGS_REDUCE_MOTION_DESC("settings.reduce_motion_desc"),

  QUALITY_LOW("settings.quality_low"),
  QUALITY_MEDIUM("settings.quality_medium"),
  QUALITY_HIGH("settings.quality_high"),
  QUALITY_ULTRA("settings.quality_ultra"),

  DIFFICULTY_EASY("settings.difficulty_easy"),
  DIFFICULTY_NORMAL("settings.difficulty_normal"),
  DIFFICULTY_HARD("settings.difficulty_hard"),
  DIFFICULTY_EXTREME("settings.difficulty_extreme"),

  COLORBLIND_NONE("settings.colorblind_none"),
  COLORBLIND_PROTANOPIA("settings.colorblind_protanopia"),
  COLORBLIND_DEUTERANOPIA("settings.colorblind_deuteranopia"),
  COLORBLIND_TRITANOPIA("settings.colorblind_tritanopia"),

  CONTROLS_DEFAULT("settings.controls_default"),
  CONTROLS_WASD("settings.controls_wasd"),
  CONTROLS_ARROWS("settings.controls_arrows"),
  CONTROLS_CUSTOM("settings.controls_custom"),

  MSG_GAME_OVER("msg.game_over"),
  MSG_GAME_OVER_SUBTITLE("msg.game_over_subtitle"),
  MSG_RESTART_GAME("msg.restart_game"),
  MSG_META_UPGRADES("msg.meta_upgrades"),
  MSG_WAVE_REACHED("msg.wave_reached"),
  MSG_SURVIVAL_TIME("msg.survival_time"),
  MSG_ENEMIES_KILLED("msg.enemies_killed"),
  MSG_META_COINS_EARNED("msg.meta_coins_earned"),
  MSG_TOTAL_META_COINS("msg.total_meta_coins"),
  MSG_VICTORY("msg.victory"),
  MSG_WAVE_COMPLETE("msg.wave_complete"),
  MSG_LOADING("msg.loading"),
  MSG_SAVING("msg.saving"),
  MSG_SAVED("msg.saved"),
  MSG_ERROR("msg.error");

  private final String key;

  TranslationKeys(String key) {
    this.key = key;
  }

  public String get() {
    return LocalizationManager.getInstance().get(key);
  }

  public String format(Object... args) {
    return LocalizationManager.getInstance().format(key, args);
  }

  public String getKey() {
    return key;
  }
}
