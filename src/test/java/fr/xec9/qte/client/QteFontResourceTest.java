package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class QteFontResourceTest {
    @Test
    void rendererSupportsBothConfiguredKeyFonts() throws Exception {
        String renderer = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/client/QteKeyPromptRenderer.java"
        ));
        assertTrue(renderer.contains("QteEngine.MOD_ID, \"qte_key_compact\""));
        assertTrue(renderer.contains("QteEngine.MOD_ID, \"qte_key_minecraft_five\""));
        assertTrue(renderer.contains("QteClientConfig.keyFont()"));
    }

    @Test
    void clientConfigDefaultsToSmallCapsAndIsRegisteredAsClientOnly() throws Exception {
        String config = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/client/QteClientConfig.java"
        ));
        String clientEntry = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/client/QteEngineClient.java"
        ));
        assertTrue(config.contains("defineEnum(\"key_font\", KeyFont.SMALL_CAPS)"));
        assertTrue(clientEntry.contains("registerConfig(ModConfig.Type.CLIENT, QteClientConfig.SPEC)"));
        assertTrue(clientEntry.contains("registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new)"));
    }

    @Test
    void smallCapsFontDelegatesToMinecraftDefaultFont() throws Exception {
        String definition = Files.readString(Path.of(
            "src/main/resources/assets/qte_engine/font/qte_key_compact.json"
        ));
        assertTrue(definition.contains("\"type\": \"reference\""));
        assertTrue(definition.contains("\"id\": \"minecraft:default\""));
        assertFalse(definition.contains("\"type\": \"bitmap\""));
        assertFalse(definition.contains("\"type\": \"ttf\""));
    }

    @Test
    void minecraftFiveModeUsesOnlyTheCompactBitmapAtlas() throws Exception {
        String definition = Files.readString(Path.of(
            "src/main/resources/assets/qte_engine/font/qte_key_minecraft_five.json"
        ));
        assertTrue(definition.contains("\"type\": \"bitmap\""));
        assertTrue(definition.contains("qte_engine:font/minecraft_five_compact.png"));
        assertFalse(definition.contains("\"type\": \"ttf\""));

        Path resources = Path.of("src/main/resources");
        assertTrue(Files.exists(resources.resolve("assets/qte_engine/textures/font/minecraft_five_compact.png")));
        assertTrue(Files.exists(resources.resolve("MINECRAFT_FIVE_OFL.txt")));
        assertFalse(Files.exists(resources.resolve("assets/qte_engine/font/minecraft-five-bold.ttf")));
        assertFalse(Files.exists(resources.resolve("assets/qte_engine/font/minecraft-five-bold.otf")));
        assertFalse(Files.exists(resources.resolve("assets/qte_engine/textures/font/minecraft_five.png")));
        assertFalse(Files.exists(resources.resolve("assets/qte_engine/textures/font/ascii.png")));
    }
}
