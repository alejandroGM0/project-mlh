package io.github.proyectoM.localization;

import io.github.proyectoM.localization.LocalizationManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for LocalizationManager.
 */
class LocalizationManagerTest {

    /**
     * Verifies that normalize language code trims lowercases and defaults blank values.
     */
    @Test
    void normalizeLanguageCodeTrimsLowercasesAndDefaultsBlankValues() {
        Assertions.assertEquals("es", LocalizationManager.normalizeLanguageCode((String)"  ES  "));
        Assertions.assertEquals("en", LocalizationManager.normalizeLanguageCode((String)""));
        Assertions.assertEquals("en", LocalizationManager.normalizeLanguageCode((String)"   "));
        Assertions.assertEquals("en", LocalizationManager.normalizeLanguageCode(null));
    }

    /**
     * Verifies that add locale if missing avoids duplicate language entries.
     */
    @Test
    void addLocaleIfMissingAvoidsDuplicateLanguageEntries() {
        ArrayList locales = new ArrayList();
        LocalizationManager.addLocaleIfMissing(locales, (String)"en");
        LocalizationManager.addLocaleIfMissing(locales, (String)"EN");
        LocalizationManager.addLocaleIfMissing(locales, (String)"fr");
        Assertions.assertEquals(2, locales.size());
        Assertions.assertEquals("en", ((Locale)locales.get(0)).getLanguage());
        Assertions.assertEquals("fr", ((Locale)locales.get(1)).getLanguage());
    }

    /**
     * Verifies that find supported locale returns matching locale when available.
     */
    @Test
    void findSupportedLocaleReturnsMatchingLocaleWhenAvailable() {
        Locale english = Locale.ENGLISH;
        Locale spanish = new Locale("es");
        List<Locale> locales = List.of(english, spanish);
        Locale result = LocalizationManager.findSupportedLocale(locales, (String)"ES");
        Assertions.assertSame(spanish, result);
    }

    /**
     * Verifies that find supported locale falls back to default language when missing.
     */
    @Test
    void findSupportedLocaleFallsBackToDefaultLanguageWhenMissing() {
        List<Locale> locales = List.of(new Locale("es"), new Locale("fr"));
        Locale result = LocalizationManager.findSupportedLocale(locales, (String)"de");
        Assertions.assertEquals("en", result.getLanguage());
    }
}