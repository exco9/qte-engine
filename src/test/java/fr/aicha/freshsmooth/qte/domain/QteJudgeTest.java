package fr.aicha.freshsmooth.qte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QteJudgeTest {
    @Test
    void reactionAcceptsExpectedKeyAndRejectsWrongKey() {
        assertEquals(QteStatus.SUCCESS, judge(QteType.REACTION, "space", 2).accept(QteInput.press("key.keyboard.space"), 2));
        assertEquals(QteStatus.FAILURE, judge(QteType.REACTION, "space", 2).accept(QteInput.press("key.keyboard.x"), 2));
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

        QteJudge success = judge(QteType.PATTERN, "w,a", 3);
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
        QteJudge precision = judge(QteType.AIM, "space", 2);
        assertEquals(QteStatus.ACTIVE, precision.accept(QteInput.axis(0.7), 2));
        assertEquals(QteStatus.SUCCESS, precision.accept(QteInput.axis(0.1), 3));
    }

    @Test
    void activeJudgeTimesOutAndEveryTypeHasAStrategy() {
        assertEquals(QteStatus.TIMEOUT, judge(QteType.REACTION, "space", 1).tick(21));
        for (QteType type : QteType.values()) {
            assertEquals(QteStatus.ACTIVE, judge(type, "space", 2).tick(1), type.name());
        }
    }

    private static QteJudge judge(QteType type, String pattern, double seconds) {
        return new QteJudge(QteDefinition.create("test", type, pattern, seconds, "say ok", null));
    }
}
