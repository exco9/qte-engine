package fr.xec9.qte.domain;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QteTerminalPolicyTest {
    @Test void serverResultReleasesAClientStillWaitingForInput() {
        UUID id = UUID.randomUUID();
        assertTrue(QteTerminalPolicy.shouldClear(id, id, false, true));
    }
    @Test void cancellationClearsEvenTerminalHudWhileResultPreservesLinger() {
        UUID id = UUID.randomUUID();
        assertTrue(QteTerminalPolicy.shouldClear(id, id, true, false));
        assertFalse(QteTerminalPolicy.shouldClear(id, id, true, true));
    }
    @Test void stalePacketsNeverClearReplacementOrMissingSession() {
        UUID id = UUID.randomUUID();
        assertFalse(QteTerminalPolicy.shouldClear(UUID.randomUUID(), id, false, false));
        assertFalse(QteTerminalPolicy.shouldClear(null, id, false, false));
    }
}
