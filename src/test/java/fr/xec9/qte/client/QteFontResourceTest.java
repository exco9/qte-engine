package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class QteFontResourceTest {
    @Test
    void everyKeyPromptUsesTheSameCompactFontAsBalance() throws Exception {
        String renderer = Files.readString(Path.of(
            "src/main/java/fr/xec9/qte/client/QteKeyPromptRenderer.java"
        ));
        assertTrue(renderer.contains("QteEngine.MOD_ID, \"qte_key_compact\""));
        assertTrue(!renderer.contains("QteEngine.MOD_ID, \"qte_key\""));
    }

    @Test
    void keyFontUsesBundledBitmapAtlasWithoutRuntimeFreeType() throws Exception {
        ClassLoader loader = QteFontResourceTest.class.getClassLoader();
        try (InputStream definition = loader.getResourceAsStream("assets/qte_engine/font/qte_key.json")) {
            assertNotNull(definition);
            String json = new String(definition.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"type\": \"bitmap\""));
            assertTrue(json.contains("qte_engine:font/minecraft_five.png"));
            assertTrue(json.contains("\"height\": 14"), "glyphs must retain enough pixels at HUD scale");
            assertTrue(json.contains("\"ascent\": 11"), "larger glyphs need a centered baseline");
        }
        try (InputStream atlasStream = loader.getResourceAsStream(
            "assets/qte_engine/textures/font/minecraft_five.png"
        )) {
            assertNotNull(atlasStream);
            BufferedImage atlas = ImageIO.read(atlasStream);
            assertNotNull(atlas);
            assertEquals(14, atlas.getWidth() / 19, "native cell width avoids texture resampling");
            assertEquals(14, atlas.getHeight() / 2, "native cell height must match provider height");
            int minY = atlas.getHeight();
            int maxY = -1;
            int cellWidth = atlas.getWidth() / 19;
            int cellHeight = atlas.getHeight() / 2;
            for (int y = 0; y < cellHeight; y++) {
                for (int x = 0; x < cellWidth; x++) {
                    if ((atlas.getRGB(x, y) >>> 24) != 0) {
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            assertEquals(11, maxY - minY + 1, "standard glyph must keep its pre-0.4.6 visual size");
            assertBinaryAlpha(atlas);
        }
        try (InputStream compactDefinition = loader.getResourceAsStream(
            "assets/qte_engine/font/qte_key_compact.json"
        ); InputStream compactAtlas = loader.getResourceAsStream(
            "assets/qte_engine/textures/font/minecraft_five_compact.png"
        )) {
            assertNotNull(compactDefinition);
            assertNotNull(compactAtlas);
            String json = new String(compactDefinition.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"height\": 14"));
            BufferedImage atlas = ImageIO.read(compactAtlas);
            assertEquals(14, atlas.getWidth() / 19);
            assertEquals(14, atlas.getHeight() / 2);
            assertEquals(10, opaqueHeight(atlas, 14, 14));
            assertBinaryAlpha(atlas);
        }
    }

    private static int opaqueHeight(BufferedImage image, int cellWidth, int cellHeight) {
        int minY = cellHeight;
        int maxY = -1;
        for (int y = 0; y < cellHeight; y++) {
            for (int x = 0; x < cellWidth; x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxY < minY ? 0 : maxY - minY + 1;
    }

    private static void assertBinaryAlpha(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getRGB(x, y) >>> 24;
                assertTrue(alpha == 0 || alpha == 255, "font edges must stay pixel-crisp");
            }
        }
    }
}
