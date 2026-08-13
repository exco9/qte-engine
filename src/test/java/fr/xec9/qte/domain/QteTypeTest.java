package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class QteTypeTest {
    @Test
    void exposesOnlySupportedTypesAndParsesCommonAliases() {
        assertArrayEquals(
            new QteType[] {
                QteType.OBSERVATION,
                QteType.REACTION_CHOICE,
                QteType.HOLD,
                QteType.MASH,
                QteType.INPUT_SEQUENCE,
                QteType.BALANCE,
                QteType.AIM,
                QteType.TRACKING
            },
            QteType.values()
        );
        assertEquals(QteType.OBSERVATION, QteType.parse("reaction"));
        assertEquals(QteType.OBSERVATION, QteType.parse("attention"));
        assertEquals(QteType.INPUT_SEQUENCE, QteType.parse("pattern"));
        assertEquals(QteType.INPUT_SEQUENCE, QteType.parse("direction"));
        assertEquals(QteType.INPUT_SEQUENCE, QteType.parse("input-sequence"));
        assertThrows(IllegalArgumentException.class, () -> QteType.parse("timing"));
        assertThrows(IllegalArgumentException.class, () -> QteType.parse("memory"));
        assertThrows(IllegalArgumentException.class, () -> QteType.parse("analog precision"));
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
