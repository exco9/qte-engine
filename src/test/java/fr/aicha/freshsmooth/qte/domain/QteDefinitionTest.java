package fr.aicha.freshsmooth.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class QteDefinitionTest {
    @Test
    void normalizesIdKeysDurationAndLeadingSlash() {
        QteDefinition definition = QteDefinition.create(
            "ancient_rune",
            QteType.INPUT_SEQUENCE,
            "W, key.keyboard.a, mouse.left",
            2.5,
            "/say %player% won",
            null
        );

        assertEquals("ancient_rune", definition.id());
        assertEquals(List.of("key.localized.w", "key.keyboard.a", "key.mouse.left"), definition.keys());
        assertEquals(50, definition.durationTicks());
        assertEquals("say %player% won", definition.resultCommand());
        assertNull(definition.texture());
        org.junit.jupiter.api.Assertions.assertFalse(definition.exclusiveInput());
    }

    @Test
    void supportsExclusiveInputCapture() {
        QteDefinition definition = QteDefinition.create(
            "locked",
            QteType.REACTION,
            "space",
            2,
            "say won",
            true,
            null
        );

        org.junit.jupiter.api.Assertions.assertTrue(definition.exclusiveInput());
    }

    @Test
    void shortLetterUsesPlayerKeyboardLayoutWhileExplicitKeyStaysPhysical() {
        QteDefinition definition = QteDefinition.create(
            "azerty",
            QteType.INPUT_SEQUENCE,
            "Z,key.keyboard.z",
            2,
            "say won",
            null
        );

        assertEquals(List.of("key.localized.z", "key.keyboard.z"), definition.keys());
    }

    @Test
    void validatesIdDurationPatternCommandAndTexture() {
        assertThrows(IllegalArgumentException.class, () -> create("Bad ID", "space", 2, "say ok", null));
        assertThrows(IllegalArgumentException.class, () -> create("ok", "", 2, "say ok", null));
        assertThrows(IllegalArgumentException.class, () -> create("ok", "space", 0.09, "say ok", null));
        assertThrows(IllegalArgumentException.class, () -> create("ok", "space", 301, "say ok", null));
        assertThrows(IllegalArgumentException.class, () -> create("ok", "space", 2, " ", null));
        assertThrows(IllegalArgumentException.class, () -> create("ok", "space", 2, "say ok", "Bad Texture"));
    }

    private static QteDefinition create(String id, String pattern, double seconds, String command, String texture) {
        return QteDefinition.create(id, QteType.REACTION, pattern, seconds, command, texture);
    }
}
