package fr.aicha.freshsmooth.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QteKeyMigrationTest {
    @Test
    void oldSingleLetterKeysBecomeLocalizedButNewAndNamedKeysStayStable() {
        assertEquals("key.localized.z", QteKeyMigration.migrate("key.keyboard.z", true));
        assertEquals("key.keyboard.z", QteKeyMigration.migrate("key.keyboard.z", false));
        assertEquals("key.keyboard.space", QteKeyMigration.migrate("key.keyboard.space", true));
        assertEquals("key.mouse.left", QteKeyMigration.migrate("key.mouse.left", true));
    }
}
