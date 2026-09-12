package fr.xec9.qte.api;

import java.util.Objects;
import java.util.UUID;

/** Immutable result emitted when a server-side QTE session terminates. */
public record QteResult(
    UUID sessionId,
    UUID playerId,
    String qteId,
    QteResultStatus status
) {
    public QteResult {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(qteId, "qteId");
        Objects.requireNonNull(status, "status");
    }
}
