package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class QteFontResourceTest {
    @Test
    void everyKeyboardPromptUsesTheResourcePackReplaceableFont() throws Exception {
        String renderer = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/client/QteKeyPromptRenderer.java"
        ));
        assertTrue(renderer.contains("QteEngine.MOD_ID, \"qte_key_compact\""));
    }

    @Test
    void qteFontDelegatesToMinecraftDefaultFont() throws Exception {
        String definition = Files.readString(Path.of(
            "src/main/resources/assets/qte_engine/font/qte_key_compact.json"
        ));
        assertTrue(definition.contains("\"type\": \"reference\""));
        assertTrue(definition.contains("\"id\": \"minecraft:default\""));
        assertFalse(definition.contains("\"type\": \"bitmap\""));
        assertFalse(definition.contains("\"type\": \"ttf\""));
    }
}
