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
    void aimUsesWholeSafeRangeAndChangesWithSessionSeed() {
        QtePointerModel.Point aimA = QtePointerModel.target(QteType.AIM, 11L, 0, 100);
        QtePointerModel.Point aimB = QtePointerModel.target(QteType.AIM, 11L, 80, 100);
        assertEquals(aimA, aimB);
        assertNotEquals(aimA, QtePointerModel.target(QteType.AIM, 12L, 0, 100));
        assertTrue(Math.abs(aimA.x()) <= 0.92);
        assertTrue(Math.abs(aimA.y()) <= 0.92);
    }

    @Test
    void trackingPathMovesAndVariesBySessionInsideFullScreenBounds() {
        QtePointerModel.Point trackingA = QtePointerModel.target(QteType.TRACKING, 21L, 0, 100);
        QtePointerModel.Point trackingB = QtePointerModel.target(QteType.TRACKING, 21L, 25, 100);
        assertNotEquals(trackingA, trackingB);
        assertNotEquals(trackingA, QtePointerModel.target(QteType.TRACKING, 22L, 0, 100));
        for (int tick = 0; tick <= 100; tick++) {
            QtePointerModel.Point point = QtePointerModel.target(QteType.TRACKING, 21L, tick, 100);
            assertTrue(Math.abs(point.x()) <= 0.92);
            assertTrue(Math.abs(point.y()) <= 0.92);
        }
    }

    @Test
    void trackingSpeedScalesMotionAndAimCanUseFixedCoordinates() {
        QtePointerModel.Point start = QtePointerModel.target(
            QteType.TRACKING, 31L, 0, 100, 0.2, null, null
        );
        QtePointerModel.Point slow = QtePointerModel.target(
            QteType.TRACKING, 31L, 20, 100, 0.2, null, null
        );
        QtePointerModel.Point fast = QtePointerModel.target(
            QteType.TRACKING, 31L, 20, 100, 1.0, null, null
        );
        assertTrue(QtePointerModel.distance(start, slow) < QtePointerModel.distance(start, fast));
        assertEquals(
            new QtePointerModel.Point(-0.4, 0.6),
            QtePointerModel.target(QteType.AIM, 31L, 50, 100, 0.45, -0.4, 0.6)
        );
    }
}
