package fr.xec9.qte.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteInput;
import fr.xec9.qte.domain.QteStatus;
import fr.xec9.qte.domain.QteType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QteServerSessionTest {
    @Test
    void clientCannotClaimSuccessBeforeServerJudgeSucceeds() {
        UUID id = UUID.randomUUID();
        QteServerSession session = new QteServerSession(id, definition(), 100);

        assertEquals(Optional.empty(), session.finish(id, 101));
        assertEquals(Optional.empty(), session.finish(UUID.randomUUID(), 101));
        assertFalse(session.matches(UUID.randomUUID()));
        assertTrue(session.matches(id));
    }

    @Test
    void matchingServerValidatedInputMakesRewardEligible() {
        UUID id = UUID.randomUUID();
        QteServerSession session = new QteServerSession(id, definition(), 100);

        assertTrue(session.accept(id, QteInput.press("key.keyboard.space"), 101));
        assertEquals(Optional.of(QteStatus.SUCCESS), session.finish(id, 101));
    }

    @Test
    void reportsAuthoritativeFailureAndTimeoutOutcomes() {
        UUID failedId = UUID.randomUUID();
        QteServerSession failed = new QteServerSession(failedId, definition(), 100);
        failed.accept(failedId, QteInput.press("key.keyboard.x"), 101);
        assertEquals(Optional.of(QteStatus.FAILURE), failed.finish(failedId, 101));

        UUID timeoutId = UUID.randomUUID();
        QteServerSession timedOut = new QteServerSession(timeoutId, definition(), 100);
        assertEquals(Optional.of(QteStatus.TIMEOUT), timedOut.finish(timeoutId, 141));
    }

    @Test
    void serverCanPollTerminalOutcomeWithoutClientFinishPacket() {
        UUID id = UUID.randomUUID();
        QteServerSession session = new QteServerSession(id, definition(), 100);

        assertEquals(Optional.empty(), session.outcome(101));
        assertEquals(Optional.of(QteStatus.TIMEOUT), session.outcome(141));
    }

    @Test
    void lateOrMalformedPointerInputIsRejected() {
        UUID id = UUID.randomUUID();
        QteServerSession session = new QteServerSession(id, definition(), 100);

        assertFalse(session.accept(id, QteInput.pointer(Double.NaN, 0), 101));
        assertFalse(session.accept(id, QteInput.press("x".repeat(65)), 101));
        assertFalse(session.accept(id, QteInput.press("key.keyboard.space"), 200));
    }

    private static QteDefinition definition() {
        return QteDefinition.create("server_test", QteType.OBSERVATION, "space", 2, "say ok", null);
    }
}
