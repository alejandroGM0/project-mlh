package io.github.proyectoM.localization;

import io.github.proyectoM.localization.TranslationKeys;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for TranslationKeys integrity.
 */
class TranslationKeysTest {

    /**
     * Verifies that every translation key is unique and non blank.
     */
    @Test
    void everyTranslationKeyIsUniqueAndNonBlank() {
        HashSet<String> keys = new HashSet<String>();
        for (TranslationKeys translationKey : TranslationKeys.values()) {
            Assertions.assertFalse(translationKey.getKey().isBlank());
            Assertions.assertFalse(keys.contains(translationKey.getKey()));
            keys.add(translationKey.getKey());
        }
    }
}