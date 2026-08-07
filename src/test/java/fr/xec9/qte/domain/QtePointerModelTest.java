package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QtePointerModelTest {
    @Test
    void relativeMovementUsesMinecraftSensitivityCurveAndInvertY() {
        QtePointerModel.Point moved = QtePointerModel.move(
            new QtePointerModel.Point(0, 0), 100, 50, 0.5, false
        );
        assertEquals(0.60, moved.x(), 0.001);
        assertEquals(0.30, moved.y(), 0.001);

        QtePointerModel.Point low = QtePointerModel.move(
            new QtePointerModel.Point(0, 0), 100, 50, 0.0, true
        );
        assertEquals(0.0384, low.x(), 0.0001);
        assertEquals(-0.0192, low.y(), 0.0001);

        assertEquals(
            new QtePointerModel.Point(1, -1),
            QtePointerModel.move(moved, 10_000, -10_000, 1.0, false)
        );
    }

    @Test
    void aimTargetIsDeterministicAndTrackingTargetMovesInsideBounds() {
        QtePointerModel.Point aimA = QtePointerModel.target(QteType.AIM, "ancient_rune", 0, 100);
        QtePointerModel.Point aimB = QtePointerModel.target(QteType.AIM, "ancient_rune", 80, 100);
        assertEquals(aimA, aimB);

        QtePointerModel.Point trackingA = QtePointerModel.target(QteType.TRACKING, "hunt", 0, 100);
        QtePointerModel.Point trackingB = QtePointerModel.target(QteType.TRACKING, "hunt", 25, 100);
        assertNotEquals(trackingA, trackingB);
        assertTrue(Math.abs(trackingB.x()) <= 0.65);
        assertTrue(Math.abs(trackingB.y()) <= 0.45);
    }
}
