package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QteJudgeTest {
    @Test
    void observationAcceptsExpectedKeyAndRejectsWrongKey() {
        assertEquals(QteStatus.SUCCESS, judge(QteType.OBSERVATION, "space", 2).accept(QteInput.press("key.keyboard.space"), 2));
        assertEquals(QteStatus.FAILURE, judge(QteType.OBSERVATION, "space", 2).accept(QteInput.press("key.keyboard.x"), 2));
    }

    @Test
    void holdSucceedsAfterSixtyPercentAndFailsOnEarlyRelease() {
        QteJudge held = judge(QteType.HOLD, "space", 1);
        assertEquals(QteStatus.ACTIVE, held.accept(QteInput.press("key.keyboard.space"), 0));
        assertEquals(QteStatus.SUCCESS, held.tick(12));

        QteJudge released = judge(QteType.HOLD, "space", 1);
        released.accept(QteInput.press("key.keyboard.space"), 0);
        assertEquals(QteStatus.FAILURE, released.accept(QteInput.release("key.keyboard.space"), 4));
    }

    @Test
    void mashNeedsCalculatedNumberOfPresses() {
        QteJudge judge = judge(QteType.MASH, "e", 1);
        for (int index = 0; index < 4; index++) {
            assertEquals(QteStatus.ACTIVE, judge.accept(QteInput.press("key.localized.e"), index));
        }
        assertEquals(QteStatus.SUCCESS, judge.accept(QteInput.press("key.localized.e"), 5));
    }

    @Test
    void sequenceAdvancesInOrderAndWrongKeyFails() {
        QteJudge sequence = judge(QteType.INPUT_SEQUENCE, "w,a,s,d", 3);
        assertEquals(QteStatus.ACTIVE, sequence.accept(QteInput.press("key.localized.w"), 1));
        assertEquals(0.25, sequence.progress(), 0.001);
        assertEquals(QteStatus.FAILURE, sequence.accept(QteInput.press("key.localized.s"), 2));

        QteJudge success = judge(QteType.INPUT_SEQUENCE, "w,a", 3);
        success.accept(QteInput.press("key.localized.w"), 1);
        assertEquals(QteStatus.SUCCESS, success.accept(QteInput.press("key.localized.a"), 2));
    }

    @Test
    void timingRequiresCenteredWindow() {
        assertEquals(QteStatus.SUCCESS, judge(QteType.TIMING, "space", 5).accept(QteInput.press("key.keyboard.space"), 70));
        assertEquals(QteStatus.FAILURE, judge(QteType.DIALOGUE_TIMING, "space", 5).accept(QteInput.press("key.keyboard.space"), 10));
    }

    @Test
    void rhythmUsesOneWindowPerPatternEntry() {
        QteJudge rhythm = judge(QteType.RHYTHM, "a,b", 5);
        assertEquals(QteStatus.ACTIVE, rhythm.accept(QteInput.press("key.localized.a"), 33));
        assertEquals(QteStatus.SUCCESS, rhythm.accept(QteInput.press("key.localized.b"), 67));
    }

    @Test
    void precisionSucceedsInsideTargetAndCanKeepAdjustingOutside() {
        QteJudge precision = judge(QteType.ANALOG_PRECISION, "space", 2);
        assertEquals(QteStatus.ACTIVE, precision.accept(QteInput.axis(0.7), 2));
        assertEquals(QteStatus.SUCCESS, precision.accept(QteInput.axis(0.1), 3));
    }

    @Test
    void aimRequiresMouseMovementIntoTargetThenConfiguredClick() {
        QteJudge aim = judge(QteType.AIM, "mouse.left", 5);
        QtePointerModel.Point target = QtePointerModel.target(QteType.AIM, "test", 10, 100);
        assertEquals(QteStatus.ACTIVE, aim.accept(QteInput.pointer(target.x(), target.y()), 10));
        assertEquals(QteStatus.SUCCESS, aim.accept(QteInput.press("key.mouse.left"), 10));

        QteJudge missed = judge(QteType.AIM, "mouse.left", 5);
        missed.accept(QteInput.pointer(target.x() >= 0 ? -1 : 1, target.y() >= 0 ? -1 : 1), 10);
        assertEquals(QteStatus.FAILURE, missed.accept(QteInput.press("key.mouse.left"), 10));
    }

    @Test
    void trackingRequiresHoldingConfiguredInputWhileFollowingMovingTarget() {
        QteJudge tracking = judge(QteType.TRACKING, "mouse.left", 1);
        tracking.accept(QteInput.press("key.mouse.left"), 0);
        for (int tick = 1; tick < tracking.trackingTargetTicks(); tick++) {
            QtePointerModel.Point target = QtePointerModel.target(QteType.TRACKING, "test", tick, 20);
            assertEquals(QteStatus.ACTIVE, tracking.accept(QteInput.pointer(target.x(), target.y()), tick));
        }
        QtePointerModel.Point target = QtePointerModel.target(
            QteType.TRACKING,
            "test",
            tracking.trackingTargetTicks(),
            20
        );
        assertEquals(
            QteStatus.SUCCESS,
            tracking.accept(QteInput.pointer(target.x(), target.y()), tracking.trackingTargetTicks())
        );

        QteJudge notHeld = judge(QteType.TRACKING, "mouse.left", 1);
        for (int tick = 1; tick <= notHeld.trackingTargetTicks(); tick++) {
            QtePointerModel.Point moving = QtePointerModel.target(QteType.TRACKING, "test", tick, 20);
            notHeld.accept(QteInput.pointer(moving.x(), moving.y()), tick);
        }
        assertEquals(QteStatus.ACTIVE, notHeld.status());
    }

    @Test
    void activeJudgeTimesOutAndEveryTypeHasAStrategy() {
        assertEquals(QteStatus.TIMEOUT, judge(QteType.OBSERVATION, "space", 1).tick(21));
        for (QteType type : QteType.values()) {
            String inputs = switch (type) {
                case INPUT_SEQUENCE, REACTION_CHOICE, MEMORY, RHYTHM -> "space,e";
                default -> "space";
            };
            assertEquals(QteStatus.ACTIVE, judge(type, inputs, 2).tick(1), type.name());
            org.junit.jupiter.api.Assertions.assertNotNull(QteJudge.strategy(type), type.name());
        }
        assertEquals(QteJudge.Strategy.SINGLE, QteJudge.strategy(QteType.OBSERVATION));
        assertEquals(QteJudge.Strategy.TIMING, QteJudge.strategy(QteType.DIALOGUE_TIMING));
        assertEquals(QteJudge.Strategy.SEQUENCE, QteJudge.strategy(QteType.INPUT_SEQUENCE));
        assertEquals(QteJudge.Strategy.RHYTHM, QteJudge.strategy(QteType.RHYTHM));
        assertEquals(QteJudge.Strategy.PRECISION, QteJudge.strategy(QteType.BALANCE));
        assertEquals(QteJudge.Strategy.AIM, QteJudge.strategy(QteType.AIM));
        assertEquals(QteJudge.Strategy.TRACKING, QteJudge.strategy(QteType.TRACKING));
        assertEquals(QteJudge.Strategy.HOLD, QteJudge.strategy(QteType.HOLD));
        assertEquals(QteJudge.Strategy.MASH, QteJudge.strategy(QteType.MASH));
    }

    private static QteJudge judge(QteType type, String pattern, double seconds) {
        return new QteJudge(QteDefinition.create("test", type, pattern, seconds, "say ok", null));
    }
}
