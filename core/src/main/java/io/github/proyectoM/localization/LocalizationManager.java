package io.github.proyectoM.localization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Centralizes game localization and language switching. */
public class LocalizationManager {
  private static final String BUNDLE_PATH = "i18n/messages";
  private static final String I18N_DIRECTORY = "i18n";
  private static final String PROPERTIES_EXTENSION = ".properties";
  private static final String LANGUAGE_PREFERENCES_NAME = "proyectoM_settings";
  private static final String LANGUAGE_PREFERENCE_KEY = "language";
  private static final String DEFAULT_LANGUAGE = "en";
  private static final String BASE_MESSAGES_FILE = "messages";
  private static final String MESSAGES_PREFIX = "messages_";
  private static final int MESSAGES_PREFIX_LENGTH = MESSAGES_PREFIX.length();

  private static final Object LOCK = new Object();
  private static LocalizationManager instance;

  private final List<Locale> availableLocales = new ArrayList<>();

  private I18NBundle bundle;
  private Locale currentLocale;
  private boolean initialized;

  private LocalizationManager() {}

  public static LocalizationManager getInstance() {
    if (instance == null) {
      synchronized (LOCK) {
        if (instance == null) {
          instance = new LocalizationManager();
          instance.initialize();
        }
      }
    }
    return instance;
  }

  public void setLocale(Locale locale) {
    Locale requiredLocale = Objects.requireNonNull(locale, "locale");
    currentLocale = requiredLocale;
    Locale bundleLocale =
        DEFAULT_LANGUAGE.equals(requiredLocale.getLanguage()) ? Locale.ROOT : requiredLocale;
    bundle = I18NBundle.createBundle(Gdx.files.internal(BUNDLE_PATH), bundleLocale);
  }

  public void setLanguage(String languageCode) {
    ensureInitialized();
    Locale locale = findSupportedLocale(availableLocales, normalizeLanguageCode(languageCode));
    setLocale(locale);
    saveLanguage(locale.getLanguage());
  }

  public String get(String key) {
    ensureInitialized();
    return bundle.get(key);
  }

  public String format(String key, Object... args) {
    ensureInitialized();
    return bundle.format(key, args);
  }

  public Locale getCurrentLocale() {
    ensureInitialized();
    return currentLocale;
  }

  public String getCurrentLanguage() {
    ensureInitialized();
    return currentLocale.getLanguage();
  }

  public Locale[] getAvailableLocales() {
    ensureInitialized();
    return availableLocales.toArray(new Locale[0]);
  }

  public String[] getAvailableLanguageNames() {
    ensureInitialized();

    String[] names = new String[availableLocales.size()];
    for (int index = 0; index < availableLocales.size(); index++) {
      Locale locale = availableLocales.get(index);
      names[index] = locale.getDisplayLanguage(locale);
    }
    return names;
  }

  public String[] getAvailableLanguageCodes() {
    ensureInitialized();

    String[] codes = new String[availableLocales.size()];
    for (int index = 0; index < availableLocales.size(); index++) {
      codes[index] = availableLocales.get(index).getLanguage();
    }
    return codes;
  }

  public void dispose() {
    bundle = null;
    currentLocale = null;
    initialized = false;
    availableLocales.clear();
    instance = null;
  }

  private void ensureInitialized() {
    if (!initialized) {
      initialize();
    }
  }

  private void initialize() {
    if (initialized) {
      return;
    }

    detectAvailableLanguages();
    setLocale(resolveInitialLocale());

    initialized = true;
  }

  private void detectAvailableLanguages() {
    availableLocales.clear();

    FileHandle i18nDirectory = Gdx.files.internal(I18N_DIRECTORY);
    if (i18nDirectory.exists() && i18nDirectory.isDirectory()) {
      for (FileHandle file : i18nDirectory.list(PROPERTIES_EXTENSION)) {
        String fileName = file.nameWithoutExtension();
        if (BASE_MESSAGES_FILE.equals(fileName)) {
          addLocaleIfMissing(availableLocales, DEFAULT_LANGUAGE);
        } else if (fileName.startsWith(MESSAGES_PREFIX)) {
          addLocaleIfMissing(availableLocales, extractLanguageCode(fileName));
        }
      }
    }

    if (availableLocales.isEmpty()) {
      addLocaleIfMissing(availableLocales, DEFAULT_LANGUAGE);
    }
  }

  private String extractLanguageCode(String fileName) {
    return fileName.substring(MESSAGES_PREFIX_LENGTH);
  }

  private Locale resolveInitialLocale() {
    String savedLanguage = getSavedLanguage();
    if (!savedLanguage.isEmpty()) {
      return findSupportedLocale(availableLocales, savedLanguage);
    }
    return findSupportedLocale(availableLocales, DEFAULT_LANGUAGE);
  }

  private void saveLanguage(String languageCode) {
    Gdx.app
        .getPreferences(LANGUAGE_PREFERENCES_NAME)
        .putString(LANGUAGE_PREFERENCE_KEY, normalizeLanguageCode(languageCode))
        .flush();
  }

  private String getSavedLanguage() {
    return Gdx.app.getPreferences(LANGUAGE_PREFERENCES_NAME).getString(LANGUAGE_PREFERENCE_KEY, "");
  }

  static String normalizeLanguageCode(String languageCode) {
    if (languageCode == null) {
      return DEFAULT_LANGUAGE;
    }

    String normalizedLanguageCode = languageCode.trim().toLowerCase(Locale.ROOT);
    if (normalizedLanguageCode.isEmpty()) {
      return DEFAULT_LANGUAGE;
    }
    return normalizedLanguageCode;
  }

  static void addLocaleIfMissing(List<Locale> locales, String languageCode) {
    String normalizedLanguageCode = normalizeLanguageCode(languageCode);
    for (Locale locale : locales) {
      if (locale.getLanguage().equals(normalizedLanguageCode)) {
        return;
      }
    }

    locales.add(new Locale(normalizedLanguageCode));
  }

  static Locale findSupportedLocale(List<Locale> locales, String languageCode) {
    String normalizedLanguageCode = normalizeLanguageCode(languageCode);
    for (Locale locale : locales) {
      if (locale.getLanguage().equals(normalizedLanguageCode)) {
        return locale;
      }
    }
    return new Locale(DEFAULT_LANGUAGE);
  }
}
