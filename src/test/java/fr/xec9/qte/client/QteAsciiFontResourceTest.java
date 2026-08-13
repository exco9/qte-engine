package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class QteAsciiFontResourceTest {
    @Test
    void asciiAtlasExposesSixteenRowsOfEightPixelGlyphs() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream atlasStream = loader.getResourceAsStream(
            "assets/qte_engine/textures/font/ascii.png"
        )) {
            assertNotNull(atlasStream);
            BufferedImage atlas = ImageIO.read(atlasStream);
            assertNotNull(atlas);
            assertEquals(128, atlas.getWidth());
            assertEquals(128, atlas.getHeight());
            assertBinaryAlpha(atlas);
        }

        try (InputStream definitionStream = loader.getResourceAsStream(
            "assets/qte_engine/font/qte_key_compact.json"
        )) {
            assertNotNull(definitionStream);
            String definition = new String(definitionStream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(definition.contains("\"type\": \"bitmap\""));
            assertTrue(definition.contains("\"file\": \"qte_engine:font/ascii.png\""));
            assertTrue(definition.contains("\"height\": 8"));
            assertTrue(definition.contains("\"ascent\": 7"));

            List<String> rows = extractCharacterRows(definition);
            assertEquals(16, rows.size());
            for (String row : rows) {
                assertEquals(16, row.codePointCount(0, row.length()));
            }
            assertEquals("ÀÁÂÈÊËÍÓÔÕÚßãõğİ", rows.get(0));
            assertEquals("ıŒœŞşŴŵžȇ\0\0\0\0\0\0\0", rows.get(1));
            assertEquals(" !\"#$%&'()*+,-./", rows.get(2));
            assertEquals("@ABCDEFGHIJKLMNO", rows.get(4));
            assertEquals("αßΓπΣσµτΦΘΩδ∞φε∩", rows.get(14));
            assertTrue(rows.get(15).endsWith("\u00A0"));
        }
    }

    private static List<String> extractCharacterRows(String definition) {
        Matcher arrayMatcher = Pattern.compile(
            "\\\"chars\\\"\\s*:\\s*\\[(.*)]\\s*}\\s*]\\s*}",
            Pattern.DOTALL
        ).matcher(definition);
        assertTrue(arrayMatcher.find(), "font definition must contain a chars array");
        String array = arrayMatcher.group(1);
        Matcher matcher = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(array);
        List<String> rows = new ArrayList<>();
        while (matcher.find()) {
            rows.add(unescapeJson(matcher.group(1)));
        }
        return rows;
    }

    private static String unescapeJson(String value) {
        Matcher unicode = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(value);
        StringBuilder result = new StringBuilder();
        while (unicode.find()) {
            unicode.appendReplacement(result, Matcher.quoteReplacement(
                Character.toString((char) Integer.parseInt(unicode.group(1), 16))
            ));
        }
        unicode.appendTail(result);
        return result.toString().replace("\\\"", "\"").replace("\\\\", "\\");
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
