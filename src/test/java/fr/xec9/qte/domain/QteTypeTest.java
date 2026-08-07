package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class QteTypeTest {
    @Test
    void parsesEveryDocumentedNameAndCommonSeparators() {
        assertEquals(QteType.OBSERVATION, QteType.parse("reaction"));
        assertEquals(QteType.OBSERVATION, QteType.parse("attention"));
        assertEquals(QteType.INPUT_SEQUENCE, QteType.parse("pattern"));
        assertEquals(QteType.INPUT_SEQUENCE, QteType.parse("direction"));
        assertEquals(QteType.INPUT_SEQUENCE, QteType.parse("input-sequence"));
        assertEquals(QteType.ANALOG_PRECISION, QteType.parse("analog precision"));
        assertEquals(QteType.DIALOGUE_TIMING, QteType.parse("DIALOGUE_TIMING"));
        assertEquals(13, QteType.values().length);
    }

    @Test
    void rejectsUnknownTypeWithUsefulMessage() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> QteType.parse("telepathy")
        );
        assertEquals("Unknown QTE type: telepathy", error.getMessage());
    }
}
