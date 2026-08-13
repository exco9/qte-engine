package fr.xec9.qte.server;

import fr.xec9.qte.domain.QteDefinition;
import fr.xec9.qte.domain.QteInput;
import fr.xec9.qte.domain.QteJudge;
import fr.xec9.qte.domain.QteStatus;
import java.util.Optional;
import java.util.UUID;

final class QteServerSession {
    private final UUID id;
    private final QteDefinition definition;
    private final long startedAt;
    private final QteJudge judge;

    QteServerSession(UUID id, QteDefinition definition, long startedAt) {
        this.id = id;
        this.definition = definition;
        this.startedAt = startedAt;
        this.judge = new QteJudge(definition, id.getMostSignificantBits() ^ id.getLeastSignificantBits());
    }

    boolean accept(UUID sessionId, QteInput input, long now) {
        if (!id.equals(sessionId) || expired(now) || !valid(input)) {
            return false;
        }
        judge.accept(input, elapsed(now));
        return true;
    }

    Optional<QteStatus> finish(UUID sessionId, long now) {
        if (!id.equals(sessionId) || expired(now)) {
            return Optional.empty();
        }
        return outcome(now);
    }

    Optional<QteStatus> outcome(long now) {
        judge.tick(elapsed(now));
        return judge.status().terminal() ? Optional.of(judge.status()) : Optional.empty();
    }

    boolean expired(long now) {
        return elapsed(now) > definition.durationTicks() + 20L;
    }

    boolean matches(UUID sessionId) {
        return id.equals(sessionId);
    }

    UUID id() {
        return id;
    }

    QteDefinition definition() {
        return definition;
    }

    private long elapsed(long now) {
        return Math.max(0, now - startedAt);
    }

    private static boolean valid(QteInput input) {
        if (input == null || input.kind() == null || input.key() == null || input.key().length() > 64) {
            return false;
        }
        return switch (input.kind()) {
            case PRESS, RELEASE -> !input.key().isBlank();
            case AXIS -> Double.isFinite(input.value()) && Math.abs(input.value()) <= 1;
            case POINTER -> Double.isFinite(input.value())
                && Double.isFinite(input.secondaryValue())
                && Math.abs(input.value()) <= 1
                && Math.abs(input.secondaryValue()) <= 1;
        };
    }
}
