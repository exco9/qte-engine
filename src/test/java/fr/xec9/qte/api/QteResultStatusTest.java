package fr.xec9.qte.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.xec9.qte.domain.QteStatus;
import org.junit.jupiter.api.Test;

class QteResultStatusTest {
    @Test
    void mapsEveryTerminalInternalStatus() {
        assertEquals(QteResultStatus.SUCCESS, QteResultStatus.fromInternal(QteStatus.SUCCESS));
        assertEquals(QteResultStatus.FAILURE, QteResultStatus.fromInternal(QteStatus.FAILURE));
        assertEquals(QteResultStatus.TIMEOUT, QteResultStatus.fromInternal(QteStatus.TIMEOUT));
    }

    @Test
    void rejectsActiveAsACompletedResult() {
        assertThrows(IllegalArgumentException.class, () -> QteResultStatus.fromInternal(QteStatus.ACTIVE));
    }
}
