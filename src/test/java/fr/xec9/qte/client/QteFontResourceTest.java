package fr.xec9.qte.client;

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
    void bothQteFontDefinitionsUseTheSameAsciiBitmap() throws Exception {
        for (String name : new String[] {"qte_key.json", "qte_key_compact.json"}) {
            String definition = Files.readString(Path.of(
                "src/main/resources/assets/qte_engine/font", name
            ));
            assertTrue(definition.contains("\"type\": \"bitmap\""));
            assertTrue(definition.contains("\"file\": \"qte_engine:font/ascii.png\""));
            assertTrue(!definition.contains("\"type\": \"ttf\""));
        }
    }
}
