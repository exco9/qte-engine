package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QteBalanceModelTest {
    @Test
    void targetArcChangesWithSessionAndStaysAwayFromStart() {
        double first = QteBalanceModel.targetPhase(41L);
        double second = QteBalanceModel.targetPhase(42L);
        assertNotEquals(first, second);
        assertTrue(first >= 0.18 && first <= 0.82);
        assertTrue(second >= 0.18 && second <= 0.82);
    }

    @Test
    void needleRotatesClockwiseAndAngularDistanceWraps() {
        assertEquals(0.0, QteBalanceModel.needlePhase(0, 100), 0.0001);
        assertEquals(0.5, QteBalanceModel.needlePhase(40, 100), 0.0001);
        assertEquals(0.04, QteBalanceModel.angularDistance(0.98, 0.02), 0.0001);
    }
}
