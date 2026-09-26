package fr.xec9.qte.server;

import static org.junit.jupiter.api.Assertions.*;
import fr.xec9.qte.api.*;
import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteType;
import java.util.*;
import org.junit.jupiter.api.Test;

class QteCompletionTest {
    @Test void everyCanonicalOutcomeDeliversEffectsAndCallbackExactlyOnce() {
        for (QteResultStatus outcome : QteResultStatus.values()) {
            UUID id = UUID.randomUUID();
            List<String> received = new ArrayList<>();
            QteServerSession session = session(id, result -> received.add(result.sessionId() + ":" + result.status()));
            session.deliver(result(id, outcome), () -> received.add("event"));
            session.deliver(result(id, QteResultStatus.CANCELLED), () -> fail("Repeated effects"));
            assertEquals(List.of("event", id + ":" + outcome), received);
            assertFalse(session.options().executeConfiguredCommands());
        }
    }
    @Test void reentrantEventCannotDeliverTwice() {
        UUID id = UUID.randomUUID();
        List<QteResultStatus> received = new ArrayList<>();
        QteServerSession session = session(id, result -> received.add(result.status()));
        session.deliver(result(id, QteResultStatus.SUCCESS), () ->
            session.deliver(result(id, QteResultStatus.CANCELLED), () -> fail("Reentrant effects")));
        assertEquals(List.of(QteResultStatus.SUCCESS), received);
    }
    @Test void throwingCallbackIsStillConsumed() {
        UUID id = UUID.randomUUID();
        int[] calls = {0};
        QteServerSession session = session(id, result -> {
            calls[0]++;
            throw new IllegalStateException("consumer failed");
        });
        assertThrows(IllegalStateException.class, () -> session.deliver(result(id, QteResultStatus.FAILURE), () -> {}));
        session.deliver(result(id, QteResultStatus.CANCELLED), () -> fail("Repeated effects"));
        assertEquals(1, calls[0]);
    }
    @Test void callbackStillRunsWhenResultEffectsThrow() {
        UUID id = UUID.randomUUID();
        List<QteResultStatus> received = new ArrayList<>();
        QteServerSession session = session(id, result -> received.add(result.status()));
        assertThrows(IllegalStateException.class, () -> session.deliver(result(id, QteResultStatus.TIMEOUT), () -> {
            throw new IllegalStateException("event failed");
        }));
        assertEquals(List.of(QteResultStatus.TIMEOUT), received);
        session.deliver(result(id, QteResultStatus.CANCELLED), () -> fail("Repeated effects"));
    }
    private static QteResult result(UUID id, QteResultStatus status) {
        return new QteResult(id, UUID.randomUUID(), "test", status);
    }
    private static QteServerSession session(UUID id, java.util.function.Consumer<QteResult> callback) {
        return new QteServerSession(id,
            QteDefinition.create("test", QteType.OBSERVATION, "space", 2, "say ok", null),
            QteRunOptions.INTEGRATION, 0, callback);
    }
}
