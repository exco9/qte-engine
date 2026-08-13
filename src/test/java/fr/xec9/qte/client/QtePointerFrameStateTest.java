package fr.xec9.qte.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.xec9.qte.domain.QtePointerModel;
import org.junit.jupiter.api.Test;

class QtePointerFrameStateTest {
    @Test
    void movementUpdatesVisiblePointerImmediatelyAndQueuesOneNetworkSample() {
        QtePointerFrameState state = new QtePointerFrameState();

        state.move(100, 50, 0.5, false);

        assertEquals(new QtePointerModel.Point(0.6, 0.3), state.pointer());
        assertTrue(state.hasPendingSample());
        assertEquals(state.pointer(), state.consumePendingSample());
        assertFalse(state.hasPendingSample());
        assertNull(state.consumePendingSample());
    }
}
