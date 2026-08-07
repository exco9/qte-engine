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
            QteType.OBSERVATION,
            "space",
            2,
            "say won",
            true,
            null
        );

        org.junit.jupiter.api.Assertions.assertTrue(definition.exclusiveInput());
    }

    @Test
    void storesSeparateNormalizedFailureResultAndHideHudOption() {
        QteDefinition definition = QteDefinition.create(
            "cinematic",
            QteType.OBSERVATION,
            "space",
            2,
            "/say success",
            "/say failure",
            true,
            true,
            null
        );

        assertEquals("say success", definition.resultCommand());
        assertEquals("say failure", definition.failureCommand());
        org.junit.jupiter.api.Assertions.assertTrue(definition.exclusiveInput());
        org.junit.jupiter.api.Assertions.assertTrue(definition.hideHud());
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
    void acceptsReadableMouseAliasesInsideMixedSequences() {
        QteDefinition definition = QteDefinition.create(
            "mouse_combo",
            QteType.INPUT_SEQUENCE,
            "m1, mouse2, left_click, right_click, mouse.middle",
            3,
            "say won",
            null
        );

        assertEquals(
            List.of(
                "key.mouse.left",
                "key.mouse.right",
                "key.mouse.left",
                "key.mouse.right",
                "key.mouse.middle"
            ),
            definition.keys()
        );
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

    @Test
    void enforcesInputCardinalityForSingleAndMultiInputMechanics() {
        assertThrows(IllegalArgumentException.class, () -> QteDefinition.create(
            "short_sequence", QteType.INPUT_SEQUENCE, "w", 2, "say ok", null
        ));
        assertThrows(IllegalArgumentException.class, () -> QteDefinition.create(
            "short_choice", QteType.REACTION_CHOICE, "w", 2, "say ok", null
        ));
        assertThrows(IllegalArgumentException.class, () -> QteDefinition.create(
            "ambiguous_observation", QteType.OBSERVATION, "w,a", 2, "say ok", null
        ));
        assertEquals(2, QteDefinition.create(
            "valid_sequence", QteType.INPUT_SEQUENCE, "w,a", 2, "say ok", null
        ).keys().size());
    }

    private static QteDefinition create(String id, String pattern, double seconds, String command, String texture) {
        return QteDefinition.create(id, QteType.OBSERVATION, pattern, seconds, command, texture);
    }
}
