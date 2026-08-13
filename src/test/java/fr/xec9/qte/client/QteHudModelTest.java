package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.xec9.qte.domain.QteType;
import org.junit.jupiter.api.Test;

class QteHudModelTest {
    @Test
    void usesCompactSubtleCountdownRing() {
        assertEquals(25, QteHudModel.RING_INNER_RADIUS);
        assertEquals(27, QteHudModel.RING_OUTER_RADIUS);
        assertEquals(190, QteHudModel.countdownAlpha(255));
        assertEquals(95, QteHudModel.countdownAlpha(128));
    }

    @Test
    void balanceKeepsItsOriginalTrackAndGreenTarget() {
        assertEquals(QteHudModel.KEY_SIZE, QteHudModel.BALANCE_KEY_SIZE);
        assertEquals(0xFFF1F4F4, QteHudModel.ACCENT_COLOR);
        assertEquals(0xFF555555, QteHudModel.BALANCE_TRACK_COLOR);
        assertEquals(0xFF70E08C, QteHudModel.BALANCE_TARGET_COLOR);
        assertEquals(24, QteHudModel.BALANCE_TRACK_INNER_RADIUS);
        assertEquals(26, QteHudModel.BALANCE_TRACK_OUTER_RADIUS);
        assertEquals(29, QteHudModel.BALANCE_DURATION_INNER_RADIUS);
        assertEquals(31, QteHudModel.BALANCE_DURATION_OUTER_RADIUS);
        assertEquals(23, QteHudModel.BALANCE_TARGET_INNER_RADIUS);
        assertEquals(27, QteHudModel.BALANCE_TARGET_OUTER_RADIUS);
    }

    @Test
    void otherQtesUseBlackSuccessProgressAtTrueThirtyFivePercentOpacity() {
        assertEquals(0xFF000000, QteHudModel.SUCCESS_INDICATOR_COLOR);
        assertEquals(89, QteHudModel.successIndicatorAlpha(255));
        assertEquals(45, QteHudModel.successIndicatorAlpha(128));
        assertEquals(20, QteHudModel.SUCCESS_PROGRESS_INNER_RADIUS);
        assertEquals(22, QteHudModel.SUCCESS_PROGRESS_OUTER_RADIUS);
        assertEquals(13, QteHudModel.SEQUENCE_SUCCESS_INNER_RADIUS);
        assertEquals(14, QteHudModel.SEQUENCE_SUCCESS_OUTER_RADIUS);
        assertEquals(15, QteHudModel.SEQUENCE_DURATION_INNER_RADIUS);
        assertEquals(17, QteHudModel.SEQUENCE_DURATION_OUTER_RADIUS);
        org.junit.jupiter.api.Assertions.assertTrue(
            QteHudModel.SUCCESS_PROGRESS_OUTER_RADIUS < QteHudModel.RING_INNER_RADIUS
        );
        org.junit.jupiter.api.Assertions.assertTrue(
            QteHudModel.SEQUENCE_SUCCESS_OUTER_RADIUS < QteHudModel.SEQUENCE_DURATION_INNER_RADIUS
        );
    }

    @Test
    void centersCompactPromptAboveHotbarAcrossScreenSizes() {
        assertEquals(new QteHudModel.Layout(86, 104, 68, 68), QteHudModel.layout(240, 220));
        assertEquals(new QteHudModel.Layout(126, 164, 68, 68), QteHudModel.layout(320, 280));
        assertEquals(new QteHudModel.Layout(366, 320, 68, 68), QteHudModel.layout(800, 436));
    }

    @Test
    void interpolatesRemainingTimeBetweenTicks() {
        assertEquals(1.0, QteHudModel.remainingFraction(0, 100, 0), 0.0001);
        assertEquals(0.495, QteHudModel.remainingFraction(50, 100, 0.5f), 0.0001);
        assertEquals(0.0, QteHudModel.remainingFraction(101, 100, 0), 0.0001);
    }

    @Test
    void easesEntryScaleAndOpacityOverTwoAndAHalfTicks() {
        assertEquals(0.0, QteHudModel.entryProgress(0, 0), 0.0001);
        assertEquals(0.488, QteHudModel.entryProgress(0, 0.5f), 0.001);
        assertEquals(1.0, QteHudModel.entryProgress(3, 0), 0.0001);
        assertEquals(0.85, QteHudModel.entryScale(0), 0.0001);
        assertEquals(1.0, QteHudModel.entryScale(1), 0.0001);
        assertEquals(0, QteHudModel.alpha(0, 255));
        assertEquals(255, QteHudModel.alpha(1, 255));
    }

    @Test
    void resultFeedbackPulsesThenFades() {
        assertEquals(1.10, QteHudModel.successScale(0), 0.0001);
        assertEquals(1.0, QteHudModel.successScale(1), 0.0001);
        assertEquals(255, QteHudModel.feedbackAlpha(0));
        assertEquals(0, QteHudModel.feedbackAlpha(8));
        assertEquals(0, QteHudModel.feedbackAlpha(20));
        assertEquals(2, Math.abs(QteHudModel.failureShake(2)));
        assertEquals(0, QteHudModel.failureShake(20));
    }

    @Test
    void classifiesEveryQteIntoItsVisualMechanic() {
        assertEquals(QteHudModel.Mechanic.BALANCE, QteHudModel.mechanic(QteType.BALANCE));
        assertEquals(QteHudModel.Mechanic.AIM, QteHudModel.mechanic(QteType.AIM));
        assertEquals(QteHudModel.Mechanic.TRACKING, QteHudModel.mechanic(QteType.TRACKING));
        assertEquals(QteHudModel.Mechanic.SEQUENCE, QteHudModel.mechanic(QteType.INPUT_SEQUENCE));
        assertEquals(QteHudModel.Mechanic.HOLD, QteHudModel.mechanic(QteType.HOLD));
        assertEquals(QteHudModel.Mechanic.MASH, QteHudModel.mechanic(QteType.MASH));
        assertEquals(QteHudModel.Mechanic.SEQUENCE, QteHudModel.mechanic(QteType.REACTION_CHOICE));
        assertEquals(QteHudModel.Mechanic.SINGLE, QteHudModel.mechanic(QteType.OBSERVATION));
    }

    @Test
    void mapsNormalizedPointerCoordinatesAcrossSafeScreenArea() {
        assertEquals(new QteHudModel.ScreenPoint(16, 16), QteHudModel.screenPoint(-1, -1, 240, 180, 16));
        assertEquals(new QteHudModel.ScreenPoint(224, 164), QteHudModel.screenPoint(1, 1, 240, 180, 16));
        assertEquals(new QteHudModel.ScreenPoint(120, 90), QteHudModel.screenPoint(0, 0, 240, 180, 16));
    }

    @Test
    void keyLabelIsCenteredAndShiftedUpWhileFollowingThePressedFace() {
        assertEquals(1, QteHudModel.keyLabelXOffset());
        assertEquals(-2, QteHudModel.keyLabelYOffset(false));
        assertEquals(0, QteHudModel.keyLabelYOffset(true));
    }

    @Test
    void keyLabelUsesThirtyFivePercentExceptOnCompactSequenceKeys() {
        assertEquals(1.35F, QteHudModel.keyLabelScale(8, 32), 0.0001F);
        assertEquals(23.0F / 24.0F, QteHudModel.keyLabelScale(24, 32), 0.0001F);
        assertEquals(1.35F, QteHudModel.keyLabelScale(0, 32), 0.0001F);
        assertEquals(1.20F, QteHudModel.keyLabelScale(8, 24), 0.0001F);
        assertEquals(15.0F / 16.0F, QteHudModel.keyLabelScale(16, 24), 0.0001F);
    }

    @Test
    void mapsPrimaryMouseButtonsToDedicatedSprites() {
        assertEquals("qte_mouse_left", QteHudModel.mousePromptSprite("M1"));
        assertEquals("qte_mouse_right", QteHudModel.mousePromptSprite("M2"));
        assertEquals("qte_mouse_mb3", QteHudModel.mousePromptSprite("M3"));
        assertEquals(null, QteHudModel.mousePromptSprite("BUTTON 4"));
        assertEquals(null, QteHudModel.mousePromptSprite("A"));
    }

    @Test
    void mousePromptSlowlyCrossfadesWithTheBaseTexture() {
        assertEquals(0, QteHudModel.mouseBlinkAlpha(0, false));
        assertEquals(128, QteHudModel.mouseBlinkAlpha(500, false));
        assertEquals(255, QteHudModel.mouseBlinkAlpha(1_000, false));
        assertEquals(128, QteHudModel.mouseBlinkAlpha(1_500, false));
        assertEquals(0, QteHudModel.mouseBlinkAlpha(2_000, false));
        assertEquals(255, QteHudModel.mouseBlinkAlpha(0, true));
    }

    @Test
    void ringSegmentCountTracksVisibleFraction() {
        assertEquals(72, QteHudModel.ringSegments(1));
        assertEquals(36, QteHudModel.ringSegments(0.5));
        assertEquals(1, QteHudModel.ringSegments(0.001));
        assertEquals(0, QteHudModel.ringSegments(0));
    }

    @Test
    void createsCompactReadableKeyLabels() {
        assertEquals("Z", QteHudModel.keyLabel("key.localized.z"));
        assertEquals("SPACE", QteHudModel.keyLabel("key.keyboard.space"));
        assertEquals("M1", QteHudModel.keyLabel("key.mouse.left"));
        assertEquals("M2", QteHudModel.keyLabel("key.mouse.right"));
        assertEquals("M3", QteHudModel.keyLabel("key.mouse.middle"));
        assertEquals("BUTTON 4", QteHudModel.keyLabel("key.mouse.4"));
        assertEquals("SHIFT", QteHudModel.keyLabel("key.keyboard.left_shift"));
        assertEquals("CTRL", QteHudModel.keyLabel("key.keyboard.left_control"));
    }

    @Test
    void clampsProgressAndSeparatesUrgencyFromExpiration() {
        assertEquals(0.0, QteHudModel.clampProgress(-0.2));
        assertEquals(0.42, QteHudModel.clampProgress(0.42));
        assertEquals(1.0, QteHudModel.clampProgress(1.4));
        assertEquals(QteHudModel.Urgency.NORMAL, QteHudModel.urgency(0.26));
        assertEquals(QteHudModel.Urgency.URGENT, QteHudModel.urgency(0.25));
        assertEquals(QteHudModel.Urgency.EXPIRED, QteHudModel.urgency(0.0));
    }

}
